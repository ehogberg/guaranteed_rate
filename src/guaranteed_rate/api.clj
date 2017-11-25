(ns guaranteed-rate.api
  (:require [guaranteed-rate.recordset :refer [transform-line-to-record]]
            [clj-time.format :as tf]))

;; Shared state for processed record storage.
;; A ref is chosen over an atom b/c this will
;; be used by a web-based, multi-threaded API.

(def processed-records (ref []))

;; Storage for processing exceptions.  Ref-based
;; for reasons similar to above.

(def processing-exceptions (ref []))



;; Private helpers for our call-level API.

(defn- add-record [r]
  (dosync (alter processed-records conj r)))

(defn- format-processing-exception [l e]
  {:original-line l
   :exception-message (.getMessage e)})

(defn- record-process-exception [l e]
  (dosync (alter processing-exceptions
                 conj (format-processing-exception l e))))

(defn- formatted-birthdate [r]
  (tf/unparse (tf/formatter "MM/dd/yyyy") (:birthdate-as-date r)))

(defn- format-record [r]
  (-> r
      (assoc :birthdate (formatted-birthdate r))
      (dissoc :birthdate-as-date)))


;; The call-level API.  Functions to...
;; a) process a line,;; store it as a completely processed record and
;; record exceptions for lines that fail processing.
;;
;; b) Clear out previously processed records and exception details.

(defn clear-processed-records [] (dosync (ref-set processed-records [])
                                         (ref-set processing-exceptions [])))


(defn process-record [l]
  (try
    (let [r (transform-line-to-record l)]
      (add-record r)
      {:new-record (format-record r)})
    (catch Exception e
      (record-process-exception l e)
      {:error (.getMessage e)
       :cause (:cause (ex-data e))})))
