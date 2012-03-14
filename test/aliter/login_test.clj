(ns aliter.login-test
  (:require [aliter.login.packets :as login])
  (:use clojure.test
        aliter.client-test))

(use-fixtures :each with-connection)

(deftest authentication
  (testing "sends refuse packet for invalid login"
    (let [response (expect login/refuse
                           login/auth
                           {:packet-version 27
                            :login "baduser"
                            :password "badpass"
                            :region 8})]
      (when response
        (is (= 1 (:reason response)))))))

