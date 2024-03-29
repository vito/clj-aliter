(ns aliter.server.login
  (:use aliter.packets
        aliter.db)
  (:require [aliter.data :as data]
            [aliter.login.packets :as login]
            [aliter.packet-server :as server])
  (:import [java.util Random]))

(defrecord LoginState [packets]
  server/PacketHandler
  (packets [this] packets)

  (handle [this state packet body respond]
    (with-db
      (let [id (get-account-id (:login body))]
        (if id
          (let [account (get-account id)
                random (Random.)
                max-id (- (Math/pow 2 31) 1)
                id-a (.nextInt random max-id)
                id-b (.nextInt random max-id)]
            (if (data/check-password (:password body) account)
              (do
                (set-login-token id-a id-b id (:packet-version body))

                (respond :accept
                         {:id-a id-a
                          :account-id id
                          :id-b id-b
                          :gender (:gender account)
                          :servers [{:ip [10 0 1 3]
                                     :port 5121
                                     :name "CLJ Aliter"
                                     :maintenance 0
                                     :new 0}]}))
              (respond :refuse {:reason 1})))
          (respond :refuse {:reason 0}))

        this))))

(defn start [port]
  (server/run port (LoginState. login/packets)))
