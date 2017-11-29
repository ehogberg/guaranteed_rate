(ns homework.recordset
  (:require [clj-time.format :refer [formatters formatter parse]]
            [clojure.string :refer [split]]))

;; The ordered field list of our record.
(def field-list [:lname :fname :gender :color :birthdate])

;; Available parsing formats for converting birthday strings to a datetype.
(def date-parsers [(formatter "MM/dd/yyyy")
                   (formatter "MM-dd-yyyy")
                   (formatters :date)
                   (formatters :basic-date)])

;; Record parsing and validation functions.

(defn- find-split-regex [l]
  (if (re-find #",|\|" l)
    #"\s?(,|\|)\s?"
    #" "))

(defn parse-line [l]
  (split l (find-split-regex l)))

(defn to-map [v]
  (if (= (count v)
         (count field-list))
    (zipmap field-list v)
    (throw (ex-info "Incomplete record"
                    {:cause :incomplete-record}))))

(defn validate-map [m]
  (if (every? #(re-find #"\S+" (get m %)) field-list)
    m
    (throw (ex-info "At least one required field missing"
                    {:cause :missing-field}))))

(defn attempt-date-parse [d fmt]
  (try
    (parse fmt d)
    (catch Exception e nil)))

;; Birthday string can arrive in a number of formats.
;; Conversion is attempted using a number of common ones and
;; uses the first one that successfully parses.
(defn convert-birthdate [m]
  (let [d (:birthdate m)
        converted-birthdate (some #(attempt-date-parse d %) date-parsers)]
    (if converted-birthdate
      (assoc m :birthdate-as-date converted-birthdate)
      (throw (ex-info
              (format "Converting birthdate %s to datetype failed" d)
              {:cause :failed-birthdate-conversion})))))


;; The pipeline that fully processes a line and converts it into a record.
(defn transform-line-to-record [l]
  (-> (parse-line l)
      (to-map)
      (validate-map)
      (convert-birthdate)))
