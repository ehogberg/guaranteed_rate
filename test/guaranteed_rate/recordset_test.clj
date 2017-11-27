(ns guaranteed-rate.recordset-test
  (:require [clj-time.format :refer [formatters parse]]
            [clojure.test :refer :all]
            [guaranteed-rate.recordset
             :refer
             [convert-birthdate parse-line to-map validate-map]]))

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

;; Missing interior info.
(def missing-interior-field-line "hogberg , eric ,   , green , 1987-01-28")

(def missing-interior-field-vec ["hogberg" "eric" " " "green" "1987-01-28"])

;; For well-intentioned but unknown delimiters, assume space-delimiting.
;; This may produce some really strange results.
(def odd-delimiter-line "hogberg * eric * male * green * 1967-09-27")

(def odd-delimiter-vec ["hogberg" "*" "eric" "*" "male" "*" "green"
                        "*" "1967-09-27"])


(deftest recordset-parse-line
  (testing "parsing space-delimited line into vector"
    (is (= (parse-line space-delimited-line) standard-vec)))
  (testing "parsing pipe-delimited line into vector"
    (is (= (parse-line pipe-delimited-line) standard-vec)))
  (testing "parsing command-delimited line into vector"
    (is (= (parse-line comma-delimited-line) standard-vec)))
  (testing "parsing line w missing field into vector"
    (is (= (parse-line missing-interior-field-line)
           missing-interior-field-vec)))
  (testing "parsing unknown delimiter line into vector"
    (is (= (parse-line odd-delimiter-line) odd-delimiter-vec))))

(deftest recordset-to-map
  (testing "known good input produces a normal map"
    (is (= (to-map standard-vec) standard-map)))
  (testing "reject a map with too many fields"
    (is (thrown? Exception
                 (to-map (conj standard-vec "someAdditionalContent")))))
  (testing "Reject a vector w/ incomplete info."
    (is (thrown? Exception  (to-map ["not" "enough" "content"]))))
  (testing "Reject a map submitted with oddly parsed info"
    (is (thrown? Exception (to-map odd-delimiter-vec)))))

(deftest valid-map
  (testing "Valid map is recognized as such"
    (is (= (validate-map standard-map) standard-map)))
  (testing "Invalid map throws exception"
    (is (thrown? Exception (validate-map {:lname "incomplete"}))))
  (testing "Map containing fields with only spaces is rejected"
    (is (thrown? Exception (validate-map {:lname "lname"
                                          :fname "fname"
                                          :gender " "
                                          :color "green"
                                          :birthdate "1987-01-01"}))))
  (testing "Nil values for fields still fail validation"
    (is (thrown? Exception (validate-map {:lname "lname"
                                          :fname "fname"
                                          :gender "male"
                                          :color "green"
                                          :birthdate nil})))))

(deftest birthday-conversion
  (testing "birthdate is properly converted to a proper Date type"
    (let [birthdate-as-date (parse (formatters :date)
                                      (:birthdate standard-map))
          map-with-birthdate (assoc standard-map
                                    :birthdate-as-date birthdate-as-date)]
      (is (= (convert-birthdate standard-map) map-with-birthdate))))
  (testing "invalid date format throws exception on conversion"
    (is (thrown? Exception
                 (convert-birthdate (assoc standard-map
                                           :birthdate "MON-DAY-YEAR"))))))
