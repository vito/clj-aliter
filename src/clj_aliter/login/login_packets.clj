(ns clj-aliter.login.login-packets
  (:use clj-aliter.packets))


(defpackets
  [16r64 ^{:response ['accept 'refuse]} auth
    "Login request from client."
    (:packet-version :int)
    (:login          (:string 24))
    (:password       (:string 24))
    (:region         :byte)]


  [16r69 ^{:request 'auth} accept
    "Accept login request."
    (:length     :short)
    (:id-a       :int)
    (:account-id :int)
    (:id-b       :int)
    (0           :int)
    (""          (:string 24))
    (0           :short)
    (:gender     :byte)
    (:servers    [(:ip          (:byte 4))
                  (:port        :short)
                  (:name        (:string 20))
                  (0            :short)
                  (:maintenance :short)
                  (:new         :short)])]

  [16r6a ^{:request 'auth} refuse
    "Refuse login request."
    (:reason :byte)
    (""      (:string 20))])
