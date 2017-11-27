(ns guaranteed-rate.api-test
  (:require [clojure.test :refer :all]
            [guaranteed-rate.api
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
          first-record (first r)
          last-record (last r)]
      (is (= "Jones" (:lname first-record)))
      (is (= "bittner" (:lname last-record)))))
  (testing "sort by birthdate"
    (let [r (get-processed-records :birthdate)
          first-record (first r)
          last-record (last r)]
      (is (= "01/28/1987" (:birthdate first-record)))
      (is (= "06/19/2006" (:birthdate last-record)))))
  (testing "sort by gender/lastname"
    (let [r (get-processed-records :gender-lname)
          first-record (first r)
          last-record (last r)]
      (is (and (= "valerie" (:fname first-record))
               (= "hogberg" (:lname first-record))))
      (is (and (= "Jones" (:lname last-record)))))))

