(ns guaranteed-rate.recordset
  (:require [clj-time.format :refer [formatters parse]]
            [clojure.string :refer [split]]))

;; The ordered field list of our record.
(def field-list [:lname :fname :gender :color :birthdate])


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

(defn convert-birthdate [m]
  (try
    (let [converted-birthdate (parse (formatters :date)
                                     (:birthdate m))]
      (assoc m :birthdate-as-date converted-birthdate))
    (catch Exception e
      (throw
       (ex-info
        (format "Converting birthdate %s to datetype failed" (:birthdate m))
        {:cause :failed-birthdate-conversion})))))


;; The pipeline that fully processes a line and converts it into a record.
(defn transform-line-to-record [l]
  (-> (parse-line l)
      (to-map)
      (validate-map)
      (convert-birthdate)))
