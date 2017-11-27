(ns guaranteed-rate.core-test
  (:require [clojure.test :refer :all]
            [clojure.string :as str]
            [clojure.tools.cli :refer [parse-opts]]
            [guaranteed-rate.core :as core]
            [guaranteed-rate.api :as api]))

(deftest cli-parsing
  (testing "invalid flags"
    (let [{:keys [errors]} (parse-opts ["-z foo.txt bar.txt baz.txt"]
                                       core/cli-options)]
      (is (some? errors))))
  (testing "specifying a file load"
    (let [{:keys [options arguments errors]}
          (parse-opts ["-p" "foo.txt" "bar.txt" "baz.txt"]
                      core/cli-options)]
      (is (:process-files options))
      (is (= 3 (count arguments)))
      (is (nil? errors))
      (is (= arguments ["foo.txt" "bar.txt" "baz.txt"])))))

(deftest process-files
  (api/clear-processed-records)
  (let [output (with-out-str
                 (core/process-options {:process-files true}
                                       ["data/test_data_spaces.txt"
                                        "no_such_file.txt"]))]
    (is (str/includes?
         output
         "test_data_spaces.txt processing complete (6 records added, 0 exceptions"))
    (is (str/includes?
         output
         "no_such_file.txt (No such file or directory)"))
    (is (str/includes?
         output
         "** Records sorted by last name (descending) **"))
    (is (str/includes?
         output
         "** Records sorted by gender/last name **"))
    (is (str/includes?
         output
         "** Records sorted by birthdate **"))))
