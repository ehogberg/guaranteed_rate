(ns homework.core-test
  (:require [clojure.string :refer [includes?]]
            [clojure.test :refer :all]
            [clojure.tools.cli :refer [parse-opts]]
            [homework.api :refer [clear-processed-records]]
            [homework.core :refer [cli-options process-options]]))

(deftest cli-parsing
  (testing "invalid flags"
    (let [{:keys [errors]} (parse-opts ["-z foo.txt bar.txt baz.txt"]
                                       cli-options)]
      (is (some? errors))))
  (testing "specifying a file load"
    (let [{:keys [options arguments errors]}
          (parse-opts ["-p" "foo.txt" "bar.txt" "baz.txt"] cli-options)]
      (is (:process-files options))
      (is (= 3 (count arguments)))
      (is (nil? errors))
      (is (= arguments ["foo.txt" "bar.txt" "baz.txt"])))))

(deftest process-files
  (clear-processed-records)
  (let [output (with-out-str
                 (process-options {:process-files true}
                                  ["data/test_data_spaces.txt"
                                   "data/test_data_errors.txt"
                                   "no_such_file.txt"]))]
    (is (includes? output
         "test_data_spaces.txt processing complete (6 records added, 0 exceptions"))
    (is (includes? output
         "test_data_errors.txt processing complete (14 records added, 6 exceptions"))
    (is (includes? output
         "no_such_file.txt (No such file or directory)"))
    (is (includes? output
         "** Records sorted by last name (descending) **"))
    (is (includes? output
         "** Records sorted by gender/last name **"))
    (is (includes? output
         "** Records sorted by birthdate **"))
    (is (includes? output
         "** Exceptions **"))))
