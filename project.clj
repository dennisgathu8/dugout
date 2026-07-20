(defproject dugout "0.1.0-SNAPSHOT"
  :description "Full-stack football analytics workbench —
                Clojure backend + ClojureScript frontend +
                shared .cljc logic. The synthesis of all
                five portfolio projects."
  :url "https://github.com/dennisgathu8/dugout"
  :license {:name "MIT"}
  :dependencies
  [[org.clojure/clojure "1.12.0"]
   ;; Backend
   [ring/ring-core "1.12.2"]
   [ring/ring-jetty-adapter "1.12.2"]
   [ring/ring-json "0.5.1"]
   [compojure "1.7.1"]
   [buddy/buddy-auth "3.0.323"]
   [buddy/buddy-sign "3.5.351"]
   ;; Database
   [com.xtdb/xtdb-core "1.24.4"]
   [com.xtdb/xtdb-rocksdb "1.24.4"]
   ;; Async + data
   [org.clojure/core.async "1.6.681"]
   [cheshire "5.13.0"]
   [com.taoensso/timbre "6.5.0"]
   [environ "1.2.0"]
   ;; ClojureScript (needed for .cljc compilation on JVM)
   [org.clojure/clojurescript "1.11.132"]]
  :plugins [[lein-environ "1.2.0"]]
  :main dugout.server
  :repl-options {:host "127.0.0.1"}
  :source-paths ["src"]
  :test-paths ["test"]
  :profiles
  {:dev {:dependencies
         [[org.clojure/test.check "1.1.1"]
          [ring/ring-mock "0.4.0"]]
         :source-paths ["src" "dev"]
         :resource-paths ["test/resources"]
         :env {:xtdb-data-path   "./xtdb-data"
               :jwt-secret       "dev-secret-min-32-chars-replace"
               :allowed-origin   "http://localhost:3000"
               :demo-mode        "true"
               :dev-mode         "true"}}
   :uberjar {:aot :all
             :omit-source true}})
