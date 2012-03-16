(ns aliter.data
  (:import [java.security MessageDigest SecureRandom]))

(defrecord Account
  [id login password salt email gender last-login last-ip])

(defrecord Char
  [id account-id num name
   job base-level base-experience job-level job-experience
   zeny str agi vit int dex luk
   max-hp hp max-sp sp
   status-points skill-points
   hair-style hair-color clothes-color
   view-weapon view-shield view-head-top view-head-middle view-head-bottom
   map x y save-map save-x save-y
   renamed effects karma manner fame
   party-id guild-id guild-position guild-taxed
   pet-id homunculus-id mercenary-id])


; generate a md5-hashed salted password
(defn hash-password [password]
  (let [md (MessageDigest/getInstance "MD5")
        rnd (SecureRandom.)
        salt (byte-array 32)]
    (.update md (.getBytes password))

    (.nextBytes rnd salt)

    [(String. (.digest md salt)) (String. salt)]))


(defn check-password [password account]
  (let [md (MessageDigest/getInstance "MD5")]
    (.update md (.getBytes password))
    (= (String. (.digest md (.getBytes (:salt account))))
       (:password account))))
