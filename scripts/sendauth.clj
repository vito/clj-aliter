(ns sendauth
  (:require [clj-aliter.login.login-packets :as login])

  (:use criterium.core
        clj-aliter.packets)

  (:import
    [java.net InetSocketAddress]
    [java.nio ByteBuffer]
    [java.nio.channels SocketChannel]))

(defn send-auth [channel encoded response]
  (.write channel encoded)
  (.read channel response)
  (.rewind encoded)
  (.rewind response))

(defn -main []
  (let [channel (SocketChannel/open (InetSocketAddress. "127.0.0.1" 2323))
        response (ByteBuffer/allocate 23)
        encoded (encode login/auth
                        {:packet-version 27
                        :login "Alex"
                        :password "sup"
                        :region 8})]
    (with-progress-reporting
      (quick-bench (send-auth channel encoded response) :verbose))

    (.close channel)))
