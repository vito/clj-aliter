(ns aliter.core
  (:require [clojure.tools.nrepl.server :as nrepl]
            [aliter.server.login :as login]))

(defn -main []
  (future (login/start 6900))
  (nrepl/start-server :port 7888))
