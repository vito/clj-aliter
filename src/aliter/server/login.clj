(ns aliter.server.login
  (:use aliter.packets)
  (:require [aliter.login.packets :as login]
            [aliter.packet-server :as server]))

(deftype LoginState [packets]
  server/PacketHandler
  (packets [this] packets)

  (handle [this packet response]
    (.write response (encode login/refuse {:reason 1}))
    this))

(defn start [port]
  (server/run (LoginState. login/packets) port))
