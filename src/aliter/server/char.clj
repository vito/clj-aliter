(ns aliter.server.char
  (:use aliter.packets
        aliter.db)
  (:require [aliter.char.packets :as packets]
            [aliter.packet-server :as server])
  (:import [java.nio ByteBuffer ByteOrder]))


(defn encode-int [i]
  (let [buf (ByteBuffer/allocate 4)]
    (.order buf ByteOrder/LITTLE_ENDIAN)
    (.putInt buf)
    (.rewind buf)
    buf))

(defmulti respond-to
  (fn [packet body handler state respond] (packet-name packet)))


(defmethod respond-to :connect [packet body handler state respond]
  ; TODO
  (respond (encode-int (:account-id body)))
  (respond :characters {:max-slots 9
                        :available-slots 9
                        :premium-slots 9
                        :characters []}))



(defrecord CharState [packets]
  server/PacketHandler
  (packets [this] packets)

  (handle [this state packet body respond]
    (println "got packet" body)
    this))


(defn start [port]
  (with-db
    (server/run port (CharState. (packets/versions 24)))))
