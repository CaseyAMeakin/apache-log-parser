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
  (fn [fmt] (-> (re-pattern input-suffix)
                (#(regex-add #"%\{([^\}]+?)\}" %))
                (re-matches fmt)
                last
                (#(str output-prefix %))
                keyword)))

(def ipv4-addr-re #"(?:\d{1,3}\.){3}\d{1,3}")
(def ipv6-addr-re #"(?:[0-9A-Fa-f]{0,4}:){2,7}(?:[0-9A-Fa-f]{0,4})")
(def ip-addr-re (regex-add ipv4-addr-re #"|" ipv6-addr-re))

;; Need to complete list of format codes here (these were the ones I needed)
(def format-strings
  `(
    [#"%a" ~ip-addr-re ~(fn [_] :remote-ip) ]
    [#"%A" ~ip-addr-re ~(fn [_] :local-ip) ]
    [#"%B" #"\d+|-" ~(fn [_] :response-bytes) ]
    [#"%b" #"\d+|-" ~(fn [_] :remote-bytes-clf) ]
    [#"%\{[^\}]+?\}C" #".*?" ~(make-inlined-keyword-getter-fn "cookie-" "C")]
    [#"%D" #"-?\d+" ~(fn [_] :time-us) ]
    [#"%\{[^\}]+?\}e" ".*?"  ~(make-inlined-keyword-getter-fn "env-" "e")]
    [#"%f" #".*?" ~(fn [_] :filename) ]
    [#"%h" #".*?" ~(fn [_] :remote-host) ]
    [#"%H" #".*?" ~(fn [_] :protocol)]
    [#"%\{[^\}]+?\}i" #".*?" ~(make-inlined-keyword-getter-fn "request-header-" "i")]
    [#"%k" #".*?" ~(fn [_] :num_keepalives)]
    [#"%l" #".*?" ~(fn [_] :remote-logname)]
    [#"%m" #".*?" ~(fn [_] :method)]
    [#"%\{[^\}]+?\}n" #".*?" ~(make-inlined-keyword-getter-fn "note-" "n")]
    [#"%\{[^\}]+?\}o" #".*?" ~(make-inlined-keyword-getter-fn "response-header-" "o")]
    [#"%p" #".*?" ~(fn [_] :server-port) ]
    [#"%\{[^\}]+?\}p" #".*?" ~(make-inlined-keyword-getter-fn "server-port" "p")]
    [#"%P" #".*?" ~(fn [_] :pid)]
    [#"%\{[^\}]+?\}P" #".*?" ~(make-inlined-keyword-getter-fn "pid-" "P")]
    [#"%q" #".*?" ~(fn [_] :query-string)]
    [#"%r" #".*?" ~(fn [_] :req-first-line)]
    [#"%R" #".*?" ~(fn [_] :handler)]
    [#"%s" #"[0-9]+?|-" ~(fn [_] :status )]
    [#"%t" #"\[.*?\]" ~(fn [_] :time) ]
    [#"%\{[^\}]+?\}t" #".*?" ~(make-inlined-keyword-getter-fn "time-" "t")]
    [#"%\{[^\}]+?\}x" #".*?" ~(make-inlined-keyword-getter-fn "extension-" "x")]
    [#"%T" #".*?" ~(fn [_] :time_s)]
    [#"%u" #".*?" ~(fn [_] :remote-user)]
    [#"%U" #".*?" ~(fn [_] :url_path)]
    [#"%v" #".*?" ~(fn [_] :server-name) ]
    [#"%V" #".*?" ~(fn [_] :server-name-2)]
    [#"%X" #".*?" ~(fn [_] :conn-status)]
    [#"%I" #".*?" ~(fn [_] :bytes-recv)]
    [#"%O" #".*?" ~(fn [_] :bytes-sent)]))

(def format-pattern-re
  (re-pattern (str "(" (str/join "|" (map #(make-regex (first %)) format-strings)) ")")))

(defn get-format-string [y]
  (def fmt-spec (first (filter (fn [x] (re-matches (make-regex (first x)) y))  format-strings)))
  (apply hash-map (interleave '(:key :fmt :regex) (cons ((last fmt-spec) y) (butlast fmt-spec)))))

(defn make-logline-re [log-format]
  (loop [gaps (str/split log-format format-pattern-re)
         fields (map #(first %)(re-seq format-pattern-re log-format))
         logline-re ""]
    (if (empty? fields) (re-pattern (str logline-re (first gaps)))
        (recur (rest gaps) (rest fields)
               (str logline-re (first gaps) "(" (:regex (get-format-string (first fields))) ")" )))))

(defn format-keywords [log-format]
  (loop [fields (map #(first %)(re-seq format-pattern-re log-format)) log-keys []]
    (if (= (count fields) 0) log-keys
        (recur (rest fields) (conj log-keys (:key (get-format-string (first fields))))))))


(defn parse-log-line [log-line my-re log-keys]
  (-> log-line
      (#(re-matches my-re %))
      rest
      (#(zipmap log-keys %))))

(defn parse-log-file [filename log-format]
  (def my-re (make-logline-re log-format))
  (def log-keys (format-keywords log-format))
  (loop [log-lines (str/split (slurp filename) #"\n") parsed-lines []]
    (if (empty? log-lines) parsed-lines
        (recur (rest log-lines)
               (-> (first log-lines)
                   (#(re-matches my-re %))
                   rest
                   (#(zipmap log-keys %))
                   (#(conj parsed-lines %)))))))



;; Example usage

;;(defn -main
;;  [& args]
;;  (println "Apache log parser test.")
;;  (def log-format "%v:%p %h %l %u %t \"%r\" %>s %O \"%{Referer}i\" \"%{User-Agent}i\"")
;;  (def my-log (parse-log-file "other_vhosts_access.log" log-format))
;;  (doseq [x my-log]
;;    (println (:user-agent x)))
;;  )
