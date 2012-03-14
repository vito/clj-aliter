(ns aliter.db
  (:require [aliter.data :as data])
  (:require [redis.core :as redis])
  
  (:import [aliter.data Account]))

(defmacro with-db [& body]
  `(redis/with-server {}
     ~@body))


(defn save-account [account]
  (redis/atomically
    (let [id (if (:id account)
                (:id account)
                (redis/incr "accounts:id"))
          acc (str "account:" id)]
      (redis/hset acc "login" (:login account))
      (redis/hset acc "password" (:password account))
      (redis/hset acc "email" (:email account))
      (redis/hset acc "gender" (:gender account))
      (redis/hset acc "last-login" (:last-login account))
      (redis/hset acc "last-ip" (:last-ip account))

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
