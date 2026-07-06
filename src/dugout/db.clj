(ns dugout.db
  "XTDB node lifecycle for dugout backend.
   Connects to temporal-squad's XTDB data store if
   XTDB_DATA_PATH is set. Falls back to in-memory
   node for demo mode."
  (:require [xtdb.api :as xt]
            [clojure.java.io :as io]
            [environ.core :refer [env]]
            [taoensso.timbre :as log]))

(defn- demo-mode? []
  (= "true" (env :demo-mode "false")))

(defn seed-demo-data! [node]
  (log/info "Seeding synthetic squad data into in-memory XTDB node...")
  (let [vt-before #inst "2026-06-01T00:00:00Z"
        vt-after  #inst "2026-06-15T00:00:00Z"
        
        ;; Players before the break
        players-before
        [{:xt/id :player/p101 :player/name "Amara Diallo" :player/fatigue-idx 0.35 :player/available? true}
         {:xt/id :player/p202 :player/name "Keza Ndayisaba" :player/fatigue-idx 0.42 :player/available? true}
         {:xt/id :player/p303 :player/name "Tendai Moyo" :player/fatigue-idx 0.71 :player/available? true}
         {:xt/id :player/p404 :player/name "Kofi Mensah" :player/fatigue-idx 0.38 :player/available? true}
         {:xt/id :player/p505 :player/name "Emeka Okafor" :player/fatigue-idx 0.60 :player/available? true}]
         
        ;; Players after the break
        players-after
        [{:xt/id :player/p101 :player/name "Amara Diallo" :player/fatigue-idx 0.67 :player/available? true}
         {:xt/id :player/p202 :player/name "Keza Ndayisaba" :player/fatigue-idx 0.58 :player/available? true}
         {:xt/id :player/p303 :player/name "Tendai Moyo" :player/fatigue-idx 0.89 :player/available? true}
         {:xt/id :player/p404 :player/name "Kofi Mensah" :player/fatigue-idx 0.41 :player/available? true}
         {:xt/id :player/p505 :player/name "Emeka Okafor" :player/fatigue-idx 0.95 :player/available? false}]
         
        tx-ops (concat
                (map (fn [p] [::xt/put p vt-before]) players-before)
                (map (fn [p] [::xt/put p vt-after]) players-after))
        tx (xt/submit-tx node (vec tx-ops))]
    (xt/await-tx node tx)
    (log/info "Synthetic squad data seeded successfully.")))

(defn start-node []
  (if (demo-mode?)
    (do (log/info "Starting XTDB in demo mode (in-memory)")
        (let [node (xt/start-node {})]
          (seed-demo-data! node)
          node))
    (let [path (or (env :xtdb-data-path)
                   (throw (ex-info "XTDB_DATA_PATH not set"
                                   {:type :dugout/config-error})))]
      (log/info "Starting XTDB node" {:path path})
      (xt/start-node
       {:xtdb/tx-log
        {:kv-store {:xtdb/module
                    'xtdb.rocksdb/->kv-store
                    :db-dir (io/file path "tx-log")}}
        :xtdb/document-store
        {:kv-store {:xtdb/module
                    'xtdb.rocksdb/->kv-store
                    :db-dir (io/file path "doc-store")}}
        :xtdb/index-store
        {:kv-store {:xtdb/module
                    'xtdb.rocksdb/->kv-store
                    :db-dir (io/file path "index-store")}}}))))

(defonce node (atom nil))

(defn get-node []
  (when-not @node
    (reset! node (start-node)))
  @node)

(defn stop-node! []
  (when @node
    (.close @node)
    (reset! node nil)))
