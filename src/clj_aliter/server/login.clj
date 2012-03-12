(ns clj-aliter.server.login
  (:use clj-aliter.packets)
  (:require [clj-aliter.login.login-packets :as login]
            [clj-aliter.packet-server :as server]))

(deftype LoginState [packets]
  server/PacketHandler
  (packets [this] packets)

  (handle [this packet response]
    (.write response (encode login/refuse {:reason 1}))
    this))

(defn start [port]
  (server/run (LoginState. login/packets) port))
