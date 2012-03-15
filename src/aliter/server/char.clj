(ns aliter.server.char
  (:use aliter.packets
        aliter.db)
  (:require [aliter.char.packets :as packets]
            [aliter.packet-server :as server]))

(defrecord CharState [packets]
  server/PacketHandler
  (packets [this] packets)

  (handle [this state packet body respond]
    (println "got packet" body)
    this))

(defn start [port]
  (with-db
    (server/run port (CharState. (packets/versions 24)))))
