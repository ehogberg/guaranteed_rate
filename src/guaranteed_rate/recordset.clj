(ns guaranteed-rate.recordset
  (:require [clojure.string :refer [split]]
            [clj-time.format :as tf]
            [clj-time.core :as time]))

(defn find-split-regex [l]
  (if (re-find #",|\|" l)
    #"\s(,|\|)\s"
    #" "))

(defn parse-line [l]
  (split l (find-split-regex l)))

(defn to-map [[lname fname gender color birthdate :as l]]
  (zipmap [:lname :fname :gender :color :birthdate] l))

(defn validate-map [m]
  (if (every? #(some? (get m %)) [:lname :fname :gender :color :birthdate])
    m
    (throw (Exception. "Validation failed"))))

(defn convert-birthdate [m]
  (let [converted-birthdate (tf/parse (tf/formatters :date)
                                      (:birthdate m))]
    (assoc m :birthdate-as-date converted-birthdate)))
