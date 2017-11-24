(ns guaranteed-rate.recordset
  (:require [clojure.string :refer [split]]))

(defn find-split-regex [l]
  (if (re-find #",|\|" l)
    #"\s(,|\|)\s"
    #" "))

(defn parse-line [l]
  (split l (find-split-regex l)))

(defn to-map [[lname fname gender color birthdate]]
  {:fname     fname
   :lname     lname
   :gender    gender
   :color     color
   :birthdate birthdate})

(defn validate-map [m]
  (if (every? #(some? (get m %)) [:lname :fname :gender :color :birthdate])
    m
    (throw (Exception. "Validation failed"))))
