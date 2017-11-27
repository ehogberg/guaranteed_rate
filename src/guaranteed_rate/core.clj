(ns guaranteed-rate.core
  (:gen-class)
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.java.io :refer [reader]]
            [guaranteed-rate.api :as api]
            [guaranteed-rate.reporting :as report]))

(def cli-options
  [["-w" "--web-service" "Start JSON web service"]
   ["-p" "--process-files" "Process files specified on command line"]
   ["-h" "--help" "Print this help"]])

(defn usage [u errors]
  (println "Usage: record-processing [-p file1 file2 file3...] [-w]")
  (println u)
  (println "If both -p and -w are specified, only -p will be run.")
  (doseq [e errors] (println e)))

(defn processing-counts [rs]
  {:records-processed (count (filter :new-record rs ))
   :exception-count (count (filter :error rs))})


;; Use of future in the file processor is a deliberate choice, 
;; allowing concurrent record loading of multiple input files.
(defn process-file [f]
  (println (format "Processing file: %s" f))
  (future
    (try
      (with-open [rdr (reader f)]
        (as-> rdr v
          (line-seq v)
          (map #(api/process-record % f) v)
          (processing-counts v)
          (assoc v :file f :processing-complete true)))
      (catch Exception e
        {:file f :err-message (.getMessage e)}))))

(defn load-files [files]
  (->> files
       (map process-file)
       (map deref)
       (report/report-file-processing-status)))

(defn process-options [options args]
  (cond
    (:process-files options) (do (load-files args)
                                 (report/generate-all-reports))
    :else (println "Nothing to do.")))

(defn run-program [args]
  (let [{:keys [errors summary options arguments]}
        (parse-opts args cli-options)
        help-requested? (:help options)]
    (if help-requested?
      (usage summary [])
      (if errors
        (usage summary errors)
        (process-options options arguments)))))

(defn -main [& args]
  (run-program args)
  (System/exit 0))
