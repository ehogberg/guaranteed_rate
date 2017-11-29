(ns homework.web-test
  (:require [clojure.test :refer :all]
            [homework.api :refer [clear-processed-records]]
            [homework.core :refer [load-files]]
            [homework.web :refer [handler]]
            [ring.mock.request :refer [body request]]))

(defn mock-api-call [method uri body-text]
  (-> (request method uri)
      (body body-text)
      (handler)))


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
  (clear-processed-records)
  (with-out-str (load-files ["data/test_data_spaces.txt"]))
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

