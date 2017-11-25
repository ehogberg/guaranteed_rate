(ns guaranteed-rate.api-test
  (:require [guaranteed-rate.api :as api]
            [clojure.test :refer :all]))

(def lines ["hogberg eric male green 1987-01-28"
            "hogberg , noah , male , purple , 2002-12-18"
            "bittner | joshua | male | red | 2000-10-31"
            "hogberg valerie female green 2006-06-19"])

(def lines-with-errors ["hogberg eric male green 1987-01-28"
                        "hogberg , noah , male , purple "
                        "hogberg | joshua | male | red | 2000-10-31"
                        "hogberg valerie female green 2006-60-19"])

(defn api-load-test-fixtures [f]
  (api/clear-processed-records)
  (f))

(use-fixtures :each api-load-test-fixtures)

(deftest api-load-lines
  (testing "testing the pipeline load (no errors)"
    (doseq [l lines] (api/process-record l))
    (is (= (count lines)
           (count (api/get-processed-records))))
    (is (= 0
           (count (api/get-processing-exceptions))))))

(deftest api-load-with-errors
  (testing "testing the pipeline load with a few errors"
    (doseq [l lines-with-errors] (api/process-record l))
    (is (= 2 (count (api/get-processed-records))))
    (is (= 2 (count (api/get-processing-exceptions))))))

