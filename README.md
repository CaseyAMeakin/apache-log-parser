# apache-log-parser

Parse an apache server log.

#### Usage

<code>
(defn -main
  [& args]
  (println "Apache log parser test!")
  (def log-format "%v:%p %h %l %u %t \"%r\" %>s %O \"%{Referer}i\" \"%{User-Agent}i\"")
  (def my-log (parse-log-file "other_vhosts_access.log" log-format))
  (doseq [x my-log] (if (not= (:request-header-Referer x) "-")
                      (println (:request-header-Referer x) ":" (:time x) ))))
</code>

#### Shortcomings

Not all the format codes have been programmed.  Needs some test code.

#### License

Nope


