(ns aliter.db
  (:require [aliter.data :as data])
  (:require [redis.core :as redis])

  (:import [aliter.data Account Char]))

(defmacro with-db [& body]
  `(redis/with-server {}
     ~@body))


(defn save-account [account]
  (redis/atomically
    (let [id (if (:id account)
                (:id account)
                (redis/incr "accounts:id"))
          acc (str "account:" id)]
      (redis/hmset acc
        "login" (:login account)
        "password" (:password account)
        "email" (:email account)
        "gender" (:gender account)
        "last-login" (:last-login account)
        "last-ip" (:last-ip account))

      (redis/hset (str "account:" (:login account)) id)))

  account)

(defn get-account [id]
  (let [attrs (redis/hgetall (str "account:" id))]
    (when attrs
      (Account.
        id
        (attrs "login")
        (attrs "password")
        (attrs "email")
        (Integer/parseInt (attrs "gender"))
        (attrs "last-login")
        (attrs "last-ip")))))

(defn get-account-id [login]
  (let [id (redis/get (str "account:" login))]
    (when id
      (Integer/parseInt id))))


(defn save-character [character]
  (redis/atomically
    (let [id (if (:id character)
                (:id character)
                (redis/incr "characters:id"))
          chr (str "character:" id)]
      (redis/hmset chr
        "id" (:id character)
        "account-id" (:account-id character)
        "num" (:num character)
        "name" (:name character)
        "job" (:job character)
        "base-level" (:base-level character)
        "base-experience" (:base-experience character)
        "job-level" (:job-level character)
        "job-experience" (:job-experience character)
        "zeny" (:zeny character)
        "str" (:str character)
        "agi" (:agi character)
        "vit" (:vit character)
        "int" (:int character)
        "dex" (:dex character)
        "luk" (:luk character)
        "max-hp" (:max-hp character)
        "hp" (:hp character)
        "max-sp" (:max-sp character)
        "sp" (:sp character)
        "status-points" (:status-points character)
        "skill-points" (:skill-points character)
        "hair-style" (:hair-style character)
        "hair-color" (:hair-color character)
        "clothes-color" (:clothes-color character)
        "view-weapon" (:view-weapon character)
        "view-shield" (:view-shield character)
        "view-head-top" (:view-head-top character)
        "view-head-middle" (:view-head-middle character)
        "view-head-bottom" (:view-head-bottom character)
        "map" (:map character)
        "x" (:x character)
        "y" (:y character)
        "save-map" (:save-map character)
        "save-x" (:save-x character)
        "save-y" (:save-y character)
        "renamed" (:renamed character)
        "effects" (:effects character)
        "karma" (:karma character)
        "manner" (:manner character)
        "fame" (:fame character)
        "party-id" (:party-id character)
        "guild-id" (:guild-id character)
        "guild-position" (:guild-position character)
        "guild-taxed" (:guild-taxed character)
        "pet-id" (:pet-id character)
        "homunculus-id" (:homunculus-id character)
        "mercenary-id" (:mercenary-id character))

      (redis/hset (str "character:" (:name character)) id)))

  character)

(defn get-character [id]
  (let [attrs (redis/hgetall (str "character:" id))]
    (when attrs
      (Char.
        (Integer/parseInt (attrs "id"))
        (Integer/parseInt (attrs "account-id"))
        (Integer/parseInt (attrs "num"))
        (attrs "name")
        (Integer/parseInt (attrs "job"))
        (Integer/parseInt (attrs "base-level"))
        (Integer/parseInt (attrs "base-experience"))
        (Integer/parseInt (attrs "job-level"))
        (Integer/parseInt (attrs "job-experience"))
        (Integer/parseInt (attrs "zeny"))
        (Integer/parseInt (attrs "str"))
        (Integer/parseInt (attrs "agi"))
        (Integer/parseInt (attrs "vit"))
        (Integer/parseInt (attrs "int"))
        (Integer/parseInt (attrs "dex"))
        (Integer/parseInt (attrs "luk"))
        (Integer/parseInt (attrs "max-hp"))
        (Integer/parseInt (attrs "hp"))
        (Integer/parseInt (attrs "max-sp"))
        (Integer/parseInt (attrs "sp"))
        (Integer/parseInt (attrs "status-points"))
        (Integer/parseInt (attrs "skill-points"))
        (Integer/parseInt (attrs "hair-style"))
        (Integer/parseInt (attrs "hair-color"))
        (Integer/parseInt (attrs "clothes-color"))
        (Integer/parseInt (attrs "view-weapon"))
        (Integer/parseInt (attrs "view-shield"))
        (Integer/parseInt (attrs "view-head-top"))
        (Integer/parseInt (attrs "view-head-middle"))
        (Integer/parseInt (attrs "view-head-bottom"))
        (attrs "map")
        (Integer/parseInt (attrs "x"))
        (Integer/parseInt (attrs "y"))
        (attrs "save-map")
        (Integer/parseInt (attrs "save-x"))
        (Integer/parseInt (attrs "save-y"))
        (Integer/parseInt (attrs "renamed"))
        (Integer/parseInt (attrs "effects"))
        (Integer/parseInt (attrs "karma"))
        (Integer/parseInt (attrs "manner"))
        (Integer/parseInt (attrs "fame"))
        (Integer/parseInt (attrs "party-id"))
        (Integer/parseInt (attrs "guild-id"))
        (Integer/parseInt (attrs "guild-position"))
        (Integer/parseInt (attrs "guild-taxed"))
        (Integer/parseInt (attrs "pet-id"))
        (Integer/parseInt (attrs "homunculus-id"))
        (Integer/parseInt (attrs "mercenary-id"))))))

(defn get-account-characters [id]
  (let [chars (redis/hgetall (str "account:" id ":chars"))]
    (map #(get-character (fnext %)) chars)))

(defn get-account-character [id num]
  (let [idx (str num)
        chars (redis/hgetall (str "account:" id ":chars"))]
    (when (contains? chars idx)
      (get-character (chars idx)))))

(defn get-character-id [name]
  (let [id (redis/get (str "character:" name))]
    (when id
      (Integer/parseInt id))))

(defn rename-character [id old-name new-name]
  (let [chr (str "char:" id)]
    (redis/atomically
      (redis/del (str "char:" old-name))
      (redis/set (str "char:" new-name) id)
      (redis/hset chr "name" new-name)
      (redis/hset chr "renamed" 1)
      true)))
