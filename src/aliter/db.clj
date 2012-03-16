(ns aliter.db
  (:require [aliter.data :as data])
  (:require [redis.core :as redis])

  (:import [aliter.data Account Char]))


(def ^:dynamic *db-settings* {})

(defmacro with-db [& body]
  `(redis/with-server *db-settings*
     ~@body))


; called after tests run to clear everything in the testing db
(defn flush-db! []
  (redis/flushdb))

(defn reset-db! []
  (flush-db!)
  (redis/set "accounts:id" 1999999)
  (redis/set "characters:id" 149999)
  true)


; set login token and have it expire after 5 minutes
(defn set-login-token [id-a id-b account-id packet-version]
  (let [tok (str "login-token:" id-a ":" id-b)]
    (redis/atomically
      (redis/hmset tok
        "account-id" account-id
        "packet-version" packet-version)
      (redis/expire tok (* 60 5)))))


; get and delete login token
(defn get-login-token [id-a id-b]
  (let [tok (str "login-token:" id-a ":" id-b)
        all (redis/hgetall tok)
        res {:account-id (Integer/parseInt (all "account-id"))
             :packet-version (Integer/parseInt (all "packet-version"))}]
    (redis/del tok)
    res))


(defn save-account [account]
  (let [id (if (:id account)
             (:id account)
             (redis/incr "accounts:id"))
        acc (str "account:" id)]
    (redis/atomically
      (redis/hmset acc
        "login" (:login account)
        "password" (:password account)
        "salt" (:salt account)
        "email" (:email account)
        "gender" (:gender account)
        "last-login" (:last-login account)
        "last-ip" (:last-ip account))

      (redis/set (str "account:" (:login account)) id))

    id))


(defn get-account [id]
  (let [attrs (redis/hgetall (str "account:" id))]
    (when attrs
      (Account.
        id
        (attrs "login")
        (attrs "password")
        (attrs "salt")
        (attrs "email")
        (Integer/parseInt (attrs "gender"))
        (attrs "last-login")
        (attrs "last-ip")))))


(defn get-account-id [login]
  (let [id (redis/get (str "account:" login))]
    (when id
      (Integer/parseInt id))))



(defn save-character [character]
  (let [id (if (:id character)
             (:id character)
             (redis/incr "characters:id"))
        chr (str "character:" id)]
    (redis/atomically
      (redis/hmset chr
        "id" id
        "account-id" (:account-id character)
        "num" (or (:num character) 0)
        "name" (or (:name character) "")
        "job" (or (:job character) 0)
        "base-level" (or (:base-level character) 1)
        "base-experience" (or (:base-experience character) 0)
        "job-level" (or (:job-level character) 1)
        "job-experience" (or (:job-experience character) 0)
        "zeny" (or (:zeny character) 0)
        "str" (or (:str character) 1)
        "agi" (or (:agi character) 1)
        "vit" (or (:vit character) 1)
        "int" (or (:int character) 1)
        "dex" (or (:dex character) 1)
        "luk" (or (:luk character) 1)
        "max-hp" (or (:max-hp character) 42)
        "hp" (or (:hp character) 42)
        "max-sp" (or (:max-sp character) 11)
        "sp" (or (:sp character) 11)
        "status-points" (or (:status-points character) 0)
        "skill-points" (or (:skill-points character) 0)
        "hair-style" (or (:hair-style character) 0)
        "hair-color" (or (:hair-color character) 0)
        "clothes-color" (or (:clothes-color character) 0)
        "view-weapon" (or (:view-weapon character) 0)
        "view-shield" (or (:view-shield character) 0)
        "view-head-top" (or (:view-head-top character) 0)
        "view-head-middle" (or (:view-head-middle character) 0)
        "view-head-bottom" (or (:view-head-bottom character) 0)
        "map" (or (:map character) "new_1-1.gat")
        "x" (or (:x character) 53)
        "y" (or (:y character) 111)
        "save-map" (or (:save-map character) "new_1-1.gat")
        "save-x" (or (:save-x character) 53)
        "save-y" (or (:save-y character) 111)
        "renamed" (or (:renamed character) 0)
        "effects" (or (:effects character) 0)
        "karma" (or (:karma character) 0)
        "manner" (or (:manner character) 0)
        "fame" (or (:fame character) 0)
        "party-id" (or (:party-id character) 0)
        "guild-id" (or (:guild-id character) 0)
        "guild-position" (or (:guild-position character) 0)
        "guild-taxed" (or (:guild-taxed character) 0)
        "pet-id" (or (:pet-id character) 0)
        "homunculus-id" (or (:homunculus-id character) 0)
        "mercenary-id" (or (:mercenary-id character) 0))

      (redis/set (str "character:" (:name character)) id)

      (redis/hset (str "account:" (:account-id character) ":chars")
                  (:num character)
                  id))

    id))


(defn get-character [id]
  (let [attrs (redis/hgetall (str "character:" id))]
    (when attrs
      (Char.
        id
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
    (map #(get-character (Integer/parseInt (fnext %))) chars)))


(defn get-account-character [id num]
  (let [idx (str num)
        chars (redis/hgetall (str "account:" id ":chars"))]
    (when (contains? chars idx)
      (get-character (Integer/parseInt (chars idx))))))


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
