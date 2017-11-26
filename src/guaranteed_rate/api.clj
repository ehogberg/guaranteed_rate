(ns guaranteed-rate.api
  (:require [guaranteed-rate.recordset :refer [transform-line-to-record]]
            [clojure.string :refer [lower-case]]
            [clj-time.format :as tf]))

;; Shared state for processed record storage.
;; A ref is chosen over an atom b/c this will
;; be used by a web-based, multi-threaded API.

(def processed-records (ref []))

;; Storage for processing exceptions.  Ref-based
;; for reasons similar to above.

(def processing-exceptions (ref []))


;; Configuration functions for use in sorting get-processed-messages
;; output.  Each possible sort type can use its own combination of
;; sorting key fields and comparison function.  When called with a
;; specific sort type, the appropriate keyfunc/comparator will be
;; looked up in this table, then fed into sort-by

(def sorting-functions {:gender-lname {:keyfunc (juxt
                                                 (comp lower-case :gender)
                                                 (comp lower-case :lname))
                                       :comparator compare}
                        :birthdate    {:keyfunc :birthdate-as-date
                                       :comparator compare}
                        :lastname     {:keyfunc (comp lower-case :lname)
                                       :comparator (comp - compare)}})


;; Private helpers for our call-level API.

(defn- add-record [r]
  (dosync (alter processed-records conj r)))

(defn- format-processing-exception [l f e]
  {:original-line l
   :original-file f
   :exception-message (.getMessage e)})

(defn- add-process-exception [l f e]
  (dosync (alter processing-exceptions
                 conj (format-processing-exception l f e))))

(defn- formatted-birthdate [r]
  (tf/unparse (tf/formatter "MM/dd/yyyy") (:birthdate-as-date r)))

(defn- format-record [r]
  (-> r
      (assoc :birthdate (formatted-birthdate r))
      (dissoc :birthdate-as-date)))


;; The call-level API.  Functions to...
;; a) process a line,
;;    store it as a completely processed record, and
;;    record exceptions for lines that fail processing.
;;
;; b) Clear out previously processed records and exception details.
;;
;; c) Get the current list of processing exceptions recorded to this point.
;;
;; d) Get the list of successfully processed records known to this point.
;;    This list can be in unsorted order, or sorted by gender/lastname,
;;    birthdate or lastname.

(defn clear-processed-records [] (dosync (ref-set processed-records [])
                                         (ref-set processing-exceptions [])))

(defn get-processed-records
  ([] (map format-record @processed-records))
  ([sort-type]
   (let [{keyfunc        :keyfunc
          comparator-fxn :comparator} (get sorting-functions sort-type)]
     (->> @processed-records
          (sort-by keyfunc comparator-fxn)
          (map format-record)))))

(defn get-processing-exceptions [] @processing-exceptions)

(defn process-record [l & [file]]
  (try
    (let [r (transform-line-to-record l)]
      (add-record r)
      {:new-record (format-record r)})
    (catch Exception e
      (add-process-exception l file e)
      {:error (.getMessage e)
       :cause (:cause (ex-data e))})))
