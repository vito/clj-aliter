(ns aliter.server.login
  (:use aliter.packets
        aliter.db)
  (:require [aliter.login.packets :as login]
            [aliter.packet-server :as server]))

(defrecord LoginState [packets]
  server/PacketHandler
  (packets [this] packets)

  (handle [this packet body respond]
    (let [id (get-account-id (:login body))]
      (if id
        (let [account (get-account id)]
          (respond login/accept
                  {:id-a 1
                    :account-id id
                    :id-b 2
                    :gender (:gender account)
                    :servers [{:ip [10 0 1 3]
                              :port 5121
                              :name "CLJ Aliter"
                              :maintenance 0
                              :new 0}]}))

        (respond login/refuse {:reason 1}))

      this)))

(defn start [port]
  (server/run (LoginState. :normal login/packets) port))
