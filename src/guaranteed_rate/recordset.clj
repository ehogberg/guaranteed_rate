(ns guaranteed-rate.recordset
  (:require [clojure.string :refer [split]]
            [clj-time.format :as tf]
            [clj-time.core :as time]))

(def field-list [:lname :fname :gender :color :birthdate])

(defn find-split-regex [l]
  (if (re-find #",|\|" l)
    #"\s(,|\|)\s"
    #" "))

(defn parse-line [l]
  (split l (find-split-regex l)))

(defn to-map [v]
  (zipmap field-list v))

(defn validate-map [m]
  (if (every? #(some? (get m %)) field-list)
    m
    (throw (ex-info "At least one required field missing"
                    {:cause :missing-field}))))

(defn convert-birthdate [m]
  (try
    (let [converted-birthdate (tf/parse (tf/formatters :date)
                                        (:birthdate m))]
      (assoc m :birthdate-as-date converted-birthdate))
    (catch Exception e (throw (ex-info
                               "Converting birthdate to datetype failed"
                               {:cause :failed-birthdate-conversion})))))
