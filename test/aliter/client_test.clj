(ns aliter.client-test
  (:use clojure.test
        aliter.packets)

  (:import
    [java.net InetSocketAddress]
    [java.nio ByteBuffer ByteOrder]
    [java.nio.channels SocketChannel]))

(defn ^:dynamic *channel*)

(defn with-connection [f]
  (binding [*channel* (SocketChannel/open (InetSocketAddress. "127.0.0.1" 2323))]
    (f)))

(defn expect [response packet data]
  (let [header-buffer (ByteBuffer/allocate 2)
        encoded (encode packet data)]
    (.write *channel* encoded)

    (.order header-buffer ByteOrder/LITTLE_ENDIAN)
    (.read *channel* header-buffer)
    (.flip header-buffer)
    
    (let [got-header (.getShort header-buffer)]
      (when (is (= got-header (header response))
                (format "received unexpected packet: 16r%x, wanted 16r%x"
                        got-header
                        (header response)))
        (let [body (ByteBuffer/allocate 1024)] ; TODO
          (.read *channel* body)
          (.flip body)
          (decode response body))))))
