(ns guaranteed-rate.web-test
  (:require [guaranteed-rate.web :as web]
            [guaranteed-rate.core :as core]
            [guaranteed-rate.api :as api]
            [ring.mock.request :as mock]
            [clojure.test :refer :all]))


(defn mock-api-call [method uri body-text]
  (-> (mock/request method uri)
      (mock/body body-text)
      (web/handler)))


(deftest post-record
  (testing "valid submission request"
    (let [{:keys [body status]} (mock-api-call
                                 :post
                                 "/records/"
                                 "hogberg eric male green 2016-01-01")]
      (is (= status 200))
      (is (:new-record body))))
  (testing "invalid post request"
    (let [{:keys [body status]} (mock-api-call
                                 :post
                                 "/records/"
                                 "two fields short")]
      (is (= status 400))
      (is (:error body)))))

(deftest get-records
  (api/clear-processed-records)
  (core/load-files ["data/test_data_spaces.txt"])
  (testing "get by birthdate"
    (let [{:keys [body]} (mock-api-call
                          :get
                          "/records/birthdate"
                          "")]
      (is (= 6 (count body)))))
  (testing "get by gender"
    (let [{:keys [body]} (mock-api-call
                          :get
                          "/records/gender"
                          "")]
      (is (= 6 (count body)))))
  (testing "get by lastname"
    (let [{:keys [body]} (mock-api-call
                          :get
                          "/records/name"
                          "")]
      (is (= 6 (count body))))))

(deftest reject-invalid-api-call
  (let [{:keys [body status]} (mock-api-call
                               :get
                               "/no/such/api"
                               "")]
    (is (= 404 status))
    (is (= "No matching route found for request" body))))

