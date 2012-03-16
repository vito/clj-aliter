(ns aliter.login-test
  (:require [aliter.login.packets :as login])
  (:use clojure.test
        aliter.db
        aliter.data
        aliter.client-test))

(use-fixtures :once
  (with-testing-server
    (fn []
      (let [[pass salt] (hash-password "goodpass")]
        (save-account {:login "gooduser"
                       :password pass
                       :salt salt
                       :email "test@example.com"
                       :gender 0
                       :last-login 0
                       :last-ip "127.0.0.1"})))))

(use-fixtures :each (with-connection 6900))

(deftest authentication
  (testing "sends accept packet for valid login"
    (let [response (expect login/accept
                           login/auth
                           {:packet-version 26
                            :login "gooduser"
                            :password "goodpass"
                            :region 8})]
      (when response
        (is true))))

  (testing "sends refuse packet for invalid user"
    (let [response (expect login/refuse
                           login/auth
                           {:packet-version 26
                            :login "baduser"
                            :password "badpass"
                            :region 8})]
      (when response
        (is (= 0 (:reason response))))))

  (testing "sends refuse packet for invalid password"
    (let [response (expect login/refuse
                           login/auth
                           {:packet-version 26
                            :login "gooduser"
                            :password "badpass"
                            :region 8})]
      (when response
        (is (= 1 (:reason response)))))))

