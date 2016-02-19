(ns apache-log-parser.core
  (:gen-class)
  (:require [clojure.string :as str]))


(defn regex-add
  "Concatenate two or more regular expressions"
  [a b & c]
  (if (empty? c) (re-pattern (str (str a ) (str b)))
      (recur (re-pattern (str (str a ) (str b))) (first c) (rest c))))

(defn make-regex [format-template-re]
  (re-pattern (str (first (str format-template-re)) "[<>]?" (apply str (rest (str format-template-re))))))

(defn make-inlined-keyword-getter-fn [output-prefix input-suffix]
  (fn [fmt] (-> input-suffix
                re-pattern
                (#(regex-add #"%\{([^\}]+?)\}" %))
                (#(re-matches % fmt) )
                last
                (#(str output-prefix %))
                keyword)))

;; Need to complete list of format codes here (these were the ones I needed)
(def format-strings
  `([#"%v" #".*?" ~(fn [_] :server-name) ]
    [#"%p" #".*?" ~(fn [_] :server-port) ]
    [#"%h" #".*?" ~(fn [_] :remote-host) ]
    [#"%l" #".*?" ~(fn [_] :remote-logname)]
    [#"%u" #".*?" ~(fn [_] :remote-user)]
    [#"%r" #".*?" ~(fn [_] :req-first-line)]
    [#"%O" #".*?" ~(fn [_] :bytes-sent)]
    [#"%t" #"\[.*?\]" ~(fn [_] :time) ]
    [#"%s" #"[0-9]+?|-" ~(fn [_] :status )]
    [#"%\{[^\}]+?\}p" #".*?" ~(fn [_] :server-port)]
    [#"%\{User-Agent\}i" #".*?" ~(fn [_] :user-agent)]
    [#"%\{[^\}]+?\}i" #".*?" ~(make-inlined-keyword-getter-fn "request-header-" "i")]))

(def format-pattern-re
  (re-pattern (str "(" (str/join "|" (map #(make-regex (first %)) format-strings)) ")")))

(defn get-format-string [y]
  (def fmt-spec (first (filter (fn [x] (re-matches (make-regex (first x)) y))  format-strings)))
  (apply hash-map (interleave '(:key :fmt :regex) (cons ((last fmt-spec) y) (butlast fmt-spec)))))

(defn make-log-line-regex [log-format]
  (loop [gaps (str/split log-format format-pattern-re)
         fields (map #(first %)(re-seq format-pattern-re log-format))
         log-line-regex ""]
    (if (empty? fields) (re-pattern (str log-line-regex (first gaps)))
        (recur (rest gaps) (rest fields)
               (str log-line-regex (first gaps) "(" (:regex (get-format-string (first fields))) ")" )))))

(defn format-keywords [log-format]
  (loop [fields (map #(first %)(re-seq format-pattern-re log-format)) log-keys []]
    (if (= (count fields) 0) log-keys
        (recur (rest fields) (conj log-keys (:key (get-format-string (first fields))))))))

(defn parse-log-file [filename log-format]
  (def my-re (make-log-line-regex log-format))
  (def log-keys (format-keywords log-format))
  (loop [log-lines (str/split (slurp filename) #"\n") parsed-lines []]
    (if (empty? log-lines) parsed-lines
        (recur (rest log-lines) (conj parsed-lines (zipmap log-keys (rest (re-matches my-re (first log-lines)))))))))

;; Example usage

(defn -main
  [& args]
  (println "Apache log parser test!")
  (def log-format "%v:%p %h %l %u %t \"%r\" %>s %O \"%{Referer}i\" \"%{User-Agent}i\"")
  (def my-log (parse-log-file "other_vhosts_access.log" log-format))
  (doseq [x my-log] (if (not= (:request-header-Referer x) "-")
                      (println (:request-header-Referer x) ":" (:time x))))
  )

