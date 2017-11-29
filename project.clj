(defproject homework "0.1.0-SNAPSHOT"
  :description "Interview technical problem homework." 
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[clj-time "0.14.2"]
                 [compojure "1.6.0"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [ring "1.6.3"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-mock "0.3.2"]]
  :main ^:skip-aot homework.core
  :aliases {"demo" ["run" "--" "-p" "data/test_data_spaces.txt"
                    "data/test_data_pipes.txt"
                    "data/test_data_commas.txt"
                    "data/test_data_errors.txt"
                    "-w"]}
  :target-path "target/%s"
  :plugins [[lein-cloverage "1.0.10"]
            [lein-ring "0.12.1"]]
  :ring {:handler homework.web/app}
  :profiles {:uberjar {:aot :all}})
