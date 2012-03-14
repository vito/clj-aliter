(ns aliter.server.login
  (:use aliter.packets
        aliter.db)
  (:require [aliter.login.packets :as login]
            [aliter.packet-server :as server]))

(defmulti respond-to (fn [s p b r] p))
(defmethod respond-to login/auth [state packet body respond]
  (let [id (get-account-id (:login body))
        account (get-account id)]
    (if id
      (do
        (respond login/accept
                 {:id-a 1
                  :account-id id
                  :id-b 2
                  :gender (:gender account)
                  :servers [{:ip [10 0 1 3]
                             :port 5121
                             :name "CLJ Aliter"
                             :maintenance 0
                             :new 0}]})
        (println "sent")
        (assoc state :mode :authenticated))
      (do
        (respond login/refuse {:reason 1})
        state))))

(defrecord LoginState [mode packets]
  server/PacketHandler
  (packets [this] packets)

  (handle [this packet body respond]
    (respond-to this packet body respond)))

(defn start [port]
  (server/run (LoginState. :normal login/packets) port))
