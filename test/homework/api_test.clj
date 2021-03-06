(ns homework.api-test
  (:require [clojure.test :refer :all]
            [homework.api
             :refer
             [clear-processed-records
              get-processed-records
              get-processing-exceptions
              process-record]]))

(def lines ["hogberg eric male green 1987-01-28"
            "Jones , colby , male , purple , 2002-12-18"
            "bittner | joshua | male | red | 2000-10-31"
            "hogberg valerie female green 2006-06-19"])

(def lines-with-errors ["hogberg eric male green 1987-01-28"
                        "hogberg , noah , male , purple "
                        "bittner | joshua | male | red | 2000-10-31"
                        "hogberg valerie female green 2006-60-19"])

(defn api-load-test-fixtures [f]
  (clear-processed-records)
  (f))

(use-fixtures :each api-load-test-fixtures)

(deftest api-load-lines
  (testing "testing the pipeline load (no errors)"
    (doseq [l lines] (process-record l))
    (is (= (count lines)
           (count (get-processed-records))))
    (is (= 0
           (count (get-processing-exceptions))))))

(deftest api-load-with-errors
  (testing "testing the pipeline load with a few errors"
    (doseq [l lines-with-errors] (process-record l))
    (is (= 2 (count (get-processed-records))))
    (is (= 2 (count (get-processing-exceptions))))))

(deftest sorting
  (doseq [l lines] (process-record l))
  (testing "sort by last name"
    (let [r (get-processed-records :lastname)
          {first-record-lname :lname} (first r)
          {last-record-lname :lname} (last r)]
      (is (= "Jones" first-record-lname))
      (is (= "bittner" last-record-lname))))
  (testing "sort by birthdate"
    (let [r (get-processed-records :birthdate)
          {first-record-birthdate :birthdate} (first r)
          {last-record-birthdate :birthdate} (last r)]
      (is (= "01/28/1987" first-record-birthdate))
      (is (= "06/19/2006" last-record-birthdate))))
  (testing "sort by gender/lastname"
    (let [r (get-processed-records :gender-lname)
          {first-record-fname :fname
           first-record-lname :lname} (first r)
          {last-record-lname :lname} (last r)]
      (is (and (= "valerie" first-record-fname)
               (= "hogberg" first-record-lname)))
      (is (and (= "Jones" last-record-lname))))))

