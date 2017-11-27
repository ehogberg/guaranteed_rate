(defproject guaranteed_rate "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-time "0.14.2"]
                 [org.clojure/tools.cli "0.3.5"]]
  :main ^:skip-aot guaranteed-rate.core
  :target-path "target/%s"
  :plugins [[lein-cloverage "1.0.10"]]
  :profiles {:uberjar {:aot :all}})
