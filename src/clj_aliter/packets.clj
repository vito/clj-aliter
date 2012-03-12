(ns clj-aliter.packets
  (:import [java.nio ByteOrder ByteBuffer]))


(defprotocol Packet
  (header [p] "The identifying number for this packet.")
  (packet-size [p d] "The size of this packet when sent with data d.")
  (encode [p d] "Encode structure d into bytes described by p.")
  (decode [p b] "Decode bytes b into structure described by p."))


(defn- segment-size [x]
  "Get the syze in bytes of a segment type."

  (cond
    (= x :long) 8
    (= x :int) 4
    (= x :short) 2
    (= x :byte) 1

    ; repeated data
    (list? x) (fnext x)

    ; repeated sub-packet
    (vector? x)
      (reduce + (map #(segment-size (fnext %)) x))))


(defn- segment-writer [buffer data size segment]
  "Write a segment to the buffer."

  (let [[name what] segment
        value (if (keyword? name)
                `(~name ~data)
                name)]
    (cond
      (= name :length) `(.putShort ~buffer ~size)

      (= what :long) `(.putLong ~buffer ~value)
      (= what :int) `(.putInt ~buffer ~value)
      (= what :short) `(.putShort ~buffer ~value)
      (= what :byte) `(.put ~buffer (byte ~value))

      ; repeated bytes
      (list? what)
        (let [[type size] what]
          `(let [end# (+ (.position ~buffer) ~size)
                 val# ~(case type
                         :string `(.getBytes ~value)
                         value)
                 bs# (byte-array (into-array (map byte val#)))]
             (.put ~buffer bs# 0 (min ~size (count bs#)))
             (.position ~buffer end#)))

      ; repeated sub-packet
      (vector? what)
        `(let [[~data] ~value]
           ~@(map #(segment-writer buffer data size %) what)))))


(defn- segment-reader [buffer segment]
  "Read a segment from the buffer, yielding the name-value pair for the
  resulting map."

  (let [[name what] segment]
    (cond
      (not (keyword? name))
        (let [padding (gensym 'padding)]
          `(~(keyword padding)
              (let [~padding ~(segment-size what)]
                (.position ~buffer (+ (.position ~buffer) ~padding))
                ~padding)))

      (= what :long) `(~name (.getLong ~buffer))
      (= what :int) `(~name (.getInt ~buffer))
      (= what :short) `(~name (.getShort ~buffer))
      (= what :byte) `(~name (.get ~buffer))

      ; repeated bytes
      (list? what)
        (let [[type size] what
              bs (gensym)]
          `(~name (let [~bs (byte-array ~size)]
                    (.get ~buffer ~bs)
                    ~(case type
                       :string `(String. ~bs)
                       bs))))

      ; repeated sub-packet
      (vector? what)
        (let [pairs (apply concat (map #(segment-reader buffer %) what))]
          `(~name (hash-map ~@pairs))))))


(defmacro make-packet [header & structure]
  "Create an anonymous type representing a given packet structure."

  (let [buffer (gensym)
        data (gensym)
        size (gensym)]
    `(reify Packet
       (header [this#] ~header)

       (packet-size [this# ~data]
         (+ 2 ~@(map (fn [x]
                       (let [size (segment-size (fnext x))]
                         (if (vector? (fnext x))
                           `(* (count (~(first x) ~data)) ~size)
                           size)))
                     structure)))

       (encode [this# ~data]
         (let [~size (packet-size this# ~data)
               ~buffer (ByteBuffer/allocate ~size)]
           (.order ~buffer ByteOrder/LITTLE_ENDIAN)
           (.putShort ~buffer (header this#))
           ~@(map #(segment-writer buffer data size %) structure)
           (.flip ~buffer)
           ~buffer))

       (decode [this# ~buffer]
         (hash-map ~@(apply concat
                            (map #(segment-reader buffer %) structure)))))))


(defmacro defpacket [header name doc & structure]
  "Helper for defining packet types."

  ; TODO: verify that this adds doc metadata, not replaces
  `(def ^{:doc ~doc} ~name
     (make-packet ~header ~@structure)))


(defmacro defpackets [& packets]
  `(do
     ~@(map (fn [x] `(defpacket ~@x)) packets)

     (def ~(symbol "packets")
        (hash-map ~@(apply concat (map #(take 2 %) packets))))))
