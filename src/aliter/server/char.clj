(ns aliter.server.char
  (:use aliter.packets
        aliter.data
        aliter.db)
  (:require [aliter.char.packets :as packets]
            [aliter.packet-server :as server])
  (:import [java.nio ByteBuffer ByteOrder]))


(defmulti respond-to
  (fn [handler state packet body respond]
    [(packet-name packet) (:mode handler)]))


(defmethod respond-to [:connect :locked] [handler state packet body respond]
  (respond (encode-single :int (:account-id body)))

  (let [account-id (:account-id body)
        session (get-login-token (:id-a body) (:id-b body))]
    (if (and session (= (:account-id session) account-id))
      (let [characters (get-account-characters account-id)
            packet-version (:packet-version session)]
        (respond :characters
                 {:max-slots 9
                  :available-slots 9
                  :premium-slots 9
                  :characters characters})

        (respond :pin-code {:account-id account-id})

        (assoc handler
               :mode :valid
               :account-id account-id))
               ;:packets (packets/versions packet-version))
      (do
        (respond :refuse {:reason 0})
        handler))))


(defmethod respond-to [:keepalive :valid] [handler state packet body respond]
  handler)


(defmethod respond-to [:create :valid] [handler state packet body respond]
  (let [exists (get-character-id (:name body))]
    (if exists
      (respond :creation-failed {:reason 0})
      (let [id (save-character (assoc (map->Char body)
                                      :account-id (:account-id handler)))
            character (get-character id)]
        (respond :character-created character))))

  handler)



(defrecord CharState [packets mode account-id]
  server/PacketHandler
  (packets [this] packets)

  (handle [this state packet body respond]
    (respond-to this state packet body respond)))


(defn start [port]
  (with-db
    (server/run port (CharState. (packets/versions 24) :locked nil))))
