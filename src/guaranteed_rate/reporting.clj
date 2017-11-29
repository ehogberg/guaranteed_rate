(ns guaranteed-rate.reporting
  (:require [guaranteed-rate.api :refer [get-processed-records
                                         get-processing-exceptions]]))


;; Status report for a file upload job
(defn report-file-processing-status [jobs]
  (doseq [job-info jobs]
    (let [{:keys [processing-complete
                  file
                  records-processed
                  exception-count
                  err-message]} job-info
          status (cond
                   processing-complete
                   (format "File %s processing complete (%d records added, %d exceptions)"
                           file records-processed exception-count)
                   err-message
                   (format "File %s encountered a fatal exception: %s"
                           file err-message))]
      (println status))))


;; Artifacts for record reporting...
(defn dashes [how-many] (->> (repeat "-") (take how-many) (apply str)))

(def table-head-line
  (format "+%s+%s+%s+%s+%s+"
          (dashes 16) (dashes 16) (dashes 9) (dashes 12) (dashes 12)))

(def format-string  "| %-14s | %-14s | %-6s  | %-10s | %-10s |")

(def report-list [{:title "Records sorted by last name (descending)"
                   :sort :lastname}
                  {:title "Records sorted by gender/last name"
                   :sort :gender-lname}
                  {:title "Records sorted by birthdate"
                   :sort :birthdate}])

(defn table-header []
  (println table-head-line)
  (println (format format-string
                   "Last Name" "First Name" "Gender" "Color" "Birthday"))
  (println table-head-line))

(defn table-footer []
  (println table-head-line)
  (newline))

(defn print-detail-line [{:keys [lname fname gender color birthdate]}]
  (println
   (format format-string lname fname gender color birthdate)))

(defn record-report [title recs]
  (println (format "** %s **\n" title))
  (table-header)
  (doseq [r recs] (print-detail-line r))
  (table-footer))

(defn exception-report [title exceptions]
  (newline)
  (println (format "** %s ** " title))
  (if (> (count exceptions) 0)
    (doseq [{:keys [original-file original-line exception-message]} exceptions]
      (println (format "File: %s\nLine: %s\nException: %s"
                       original-file original-line exception-message))
      (newline))
    (println "No exceptions recorded.")))

(defn generate-all-reports []
  (doseq [{:keys [title sort]} report-list]
    (newline)
    (->> sort
         (get-processed-records)
         (record-report title)))
  (exception-report "Exceptions" (get-processing-exceptions)))
