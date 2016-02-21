(defproject apache-log-parser "0.1.0"
  :description "Code to parse an apache webserver log file"
  :url "https://github.com/CaseyAMeakin/apache-log-parser"
  :license "none"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [clj-detector "0.0.2"]
                 [net.sf.uadetector/uadetector-core "0.9.10"]
                 [net.sf.uadetector/uadetector-resources "2013.10"]
                 [org.slf4j/slf4j-simple "1.7.5"]]
  :main apache-log-parser.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
