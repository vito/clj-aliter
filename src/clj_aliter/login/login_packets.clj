(ns clj-aliter.login.login-packets
  (:use clj-aliter.packets
        clojure.pprint
        hexdump.core))


(defpacket 16r64 ^{:response 'accept} auth
  "Login request from client."
  (:packet-version :int)
  (:login          (:string 24))
  (:password       (:string 24))
  (:region         :byte))

(defpacket 16r69 ^{:request 'auth} accept
  "Accept login request."
  (:length     :short)
  (:id-a       :int)
  (:account-id :int)
  (:id-b       :int)
  (0           :int)
  ([]          (:bytes 24)) ; TODO: ?
  (0           :short)
  (:gender     :byte)
  (:servers    [(:ip          (:byte 4))
                (:port        :short)
                (:name        (:string 20))
                (0            :short)
                (:maintenance :short)
                (:new         :short)]))

(let [encoded (encode auth
                      {:packet-version 27
                       :login "Alex"
                       :password "sup"
                       :region 8})]
  (println "hex:")
  (hexdump (seq (.array encoded)))
  (println "structure:")
  (pprint (decode auth encoded))
  (println ""))

(let [encoded (encode accept
                      {:id-a 1
                       :account-id 2000000
                       :id-b 2
                       :gender 1
                       :servers [{:ip [10 0 1 3]
                                  :port 5121
                                  :name "Aliter"
                                  :maintenance 0
                                  :new 0}]})]
  (println "hex:")
  (hexdump (seq (.array encoded)))
  (println "structure:")
  (pprint (decode accept encoded)))
