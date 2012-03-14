(ns aliter.db
  (:require [aliter.data :as data])
  (:require [redis.core :as redis])
  
  (:import [aliter.data Account]))

(defmacro with-redis [& body]
  `(redis/with-server {}
     ~@body))


(defn save-account [account]
  (with-redis
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

        (redis/hset (str "account:" (:login account)) id))))

  account)

(defn get-account [id]
  (with-redis
    (let [acc (str "account:" id)]
      (Account.
        id
        (redis/hget acc "login")
        (redis/hget acc "password")
        (redis/hget acc "email")
        (Integer/parseInt (redis/hget acc "gender"))
        (redis/hget acc "last-login")
        (redis/hget acc "last-ip")))))

(defn get-account-id [login]
  (with-redis
    (Integer/parseInt (redis/get (str "account:" login)))))
