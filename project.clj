(defproject aliter "0.1.0-SNAPSHOT"
  :description "Experimental port of Aliter to Clojure."
  :url "http://projectaliter.com/"

  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojars.tavisrudd/redis-clojure "1.3.1"]]

  :profiles {:dev {:source-paths ["scripts" "src" "test"]
                   :dependencies [[criterium "0.2.1-SNAPSHOT"]]}}

  :main aliter.core)
