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
