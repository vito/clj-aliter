(ns sendauth
  (:require [aliter.login.packets :as login])

  (:use criterium.core
        aliter.packets)

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
  (let [channel (SocketChannel/open (InetSocketAddress. "127.0.0.1" 6900))
        response (ByteBuffer/allocate 100)
        encoded (encode login/auth
                        {:packet-version 27
                        :login "Alex"
                        :password "sup"
                        :region 8})]
    (with-progress-reporting
      (quick-bench (send-auth channel encoded response) :verbose))

    (.close channel)))
