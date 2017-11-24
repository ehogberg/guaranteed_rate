(ns guaranteed-rate.recordset-test
  (:require [guaranteed-rate.recordset :as rs]
            [clojure.test :refer :all]))

;; Expected outputs from known valid inputs.
(def standard-vec ["hogberg" "eric" "male" "green" "1987-01-28"])

(def standard-map {:fname "eric"
                   :lname "hogberg"
                   :gender "male"
                   :color  "green"
                   :birthdate "1987-01-28"})


;; Known good inputs.
(def space-delimited-line "hogberg eric male green 1987-01-28")

(def pipe-delimited-line "hogberg | eric | male | green | 1987-01-28")

(def comma-delimited-line  "hogberg , eric , male , green , 1987-01-28")


;; For well-intentioned but unknown delimiters, assume space-delimiting.
;; This may produce some really strange results.
(def odd-delimiter-line "hogberg * eric * male * green * 1967-09-27")
(def odd-delimiter-vec ["hogberg" "*" "eric" "*" "male" "*" "green"
                        "*" "1967-09-27"])


(deftest recordset-parse-line
  (testing "parsing space-delimited line into vector"
    (is (= (rs/parse-line space-delimited-line) standard-vec)))
  (testing "parsing pipe-delimited line into vector"
    (is (= (rs/parse-line pipe-delimited-line) standard-vec)))
  (testing "parsing command-delimited line into vector"
    (is (= (rs/parse-line comma-delimited-line) standard-vec)))
  (testing "parsing unknown delimiter line into vector"
    (is (= (rs/parse-line odd-delimiter-line) odd-delimiter-vec))))

(deftest recordset-to-map
  (testing "known good input produces a normal map"
    (is (= (rs/to-map standard-vec) standard-map)))
  (testing "take only the first five fields supplied"
    (is (= (rs/to-map (conj standard-vec "someAdditionalContent"))
           standard-map))))
