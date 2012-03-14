(ns aliter.char.packets
  (:use aliter.packets)

  (:require [aliter.char.packets-24 :as packets-24]))

(def versions
  {24 packets-24/packets})
