(defproject apache-log-parser "0.1.0"
  :description "Code to parse an apache webserver log file"
  :url "https://github.com/CaseyAMeakin/apache-log-parser"
  :license "none"
  :dependencies [[org.clojure/clojure "1.7.0"]]
  :main apache-log-parser.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
