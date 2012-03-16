(ns aliter.client-test
  (:use clojure.test
        aliter.packets)

  (:require [aliter.db :as db]
            [aliter.core :as core])

  (:import
    [java.net InetSocketAddress]
    [java.nio ByteBuffer ByteOrder]
    [java.nio.channels SocketChannel]))

(def ^:dynamic *channel*)

(defn with-connection [port]
  (fn [f]
    (binding [*channel* (SocketChannel/open
                          (InetSocketAddress. "127.0.0.1" port))]
      (f))))

(defn with-testing-server [setup]
  (fn [f]
    (binding [db/*db-settings* {:db 15}]
      (let [[l c] (core/start-servers)]
        (db/with-db
          (db/reset-db!)
          (setup))

        (f)

        (db/with-db (db/flush-db!))

        (future-cancel c)
        (future-cancel l)))))

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
