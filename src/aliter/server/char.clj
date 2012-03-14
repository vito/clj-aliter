(ns aliter.server.char
  (:use aliter.packets
        aliter.db)
  (:require [aliter.char.packets :as packets]
            [aliter.packet-server :as server]))

(defrecord CharState [packets]
  server/PacketHandler
  (packets [this] packets)

  (handle [this packet body respond]
    (println "got packet" body)
    this))

(defn start [port]
  (with-db
    (server/run (CharState. (packets/versions 24)) port)))
