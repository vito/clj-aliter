(ns clj-aliter.login-test
  (:require [clj-aliter.login.login-packets :as login])
  (:use clojure.test
        clj-aliter.client-test))

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

