(ns aliter.core
  (:require [clojure.tools.nrepl.server :as nrepl]
            [aliter.server.login :as login]
            [aliter.server.char :as char]))

(defn start-servers []
  [(future (login/start 6900))
   (future (char/start 5121))])

(defn -main []
  (start-servers)
  (nrepl/start-server :port 7888))
