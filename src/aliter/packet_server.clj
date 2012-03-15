(ns aliter.packet-server
  (:require [aliter.login.packets :as login])

  (:use aliter.packets
        clojure.pprint)

  (:import [java.net InetSocketAddress]
           [java.nio ByteOrder ByteBuffer]
           [java.nio.channels ServerSocketChannel Selector SelectionKey]))


(defprotocol PacketHandler
  (packets [ph] "The packets handled by this handler.")
  (handle [ph state packet body response]
          "Handle a packet, returning the new handler state."))


(defn selector [ssc]
  (let [selector (Selector/open)]
    (.register ssc selector SelectionKey/OP_ACCEPT)
    selector))


(defn setup [port]
  (let [ssc (ServerSocketChannel/open)]
    (.configureBlocking ssc false)

    (let [sock (.socket ssc)
          addr (InetSocketAddress. port)]
      (.bind sock addr)
      [(selector ssc) sock])))


(defn accept-connection [handler server-socket selector]
  (let [channel (.getChannel (.accept server-socket))]
    (println "Connected to" channel)
    (.configureBlocking channel false)
    (let [state (agent handler)]
      (set-error-handler! state
                          (fn [a e]
                            (println (.getMessage e))
                            (.printStackTrace e)
                            (.close (.socket channel))))
      (.register channel selector SelectionKey/OP_READ state))))


(defn disconnected [selected-key]
  (println "Lost connection with" (.channel selected-key))
  (.cancel selected-key)
  (.close (.socket (.channel selected-key))))


(defn handle-packet [state selected-key channel header]
  (let [handler (.attachment selected-key)
        packets (packets (deref handler))
        packet (packets header)
        body (ByteBuffer/allocate 1024)] ; TODO
    (.order body ByteOrder/LITTLE_ENDIAN)
    (.read channel body)
    (.flip body)
    (if packet
      (send handler handle
            state
            packet
            (decode packet body)
            (fn
              ([send-name send-body]
               (let [send-packet (packets send-name)
                     encoded (encode send-packet send-body)]
                 (.write channel encoded)))
              ([send-data]
               (.write channel send-data))))
      (println (format "Unknown packet: 16r%x" header)))))


(defn read-socket [state selected-key]
  (let [channel (.channel selected-key)
        header-buffer (ByteBuffer/allocate 2)]
    (.order header-buffer ByteOrder/LITTLE_ENDIAN)
    (.read channel header-buffer)
    (.flip header-buffer)

    (if (= (.limit header-buffer) 0)
      (disconnected selected-key)
      (handle-packet state selected-key channel (.getShort header-buffer)))))


(defn ready-for? [operation channel]
  (= (bit-and (.readyOps channel) operation) operation))


(defn react [state handler selector server-socket]
  (while true
    (when (> (.select selector) 0)
      (let [selected-keys (.selectedKeys selector)]
        (doseq [k selected-keys]
          (condp ready-for? k
            SelectionKey/OP_ACCEPT
              (accept-connection handler server-socket selector)

            SelectionKey/OP_READ
              (read-socket state k)))

        (.clear selected-keys)))))


(defn run
  ([port handler state]
    (apply react (agent state) handler (setup port)))

  ([port handler]
    (apply react nil handler (setup port))))
