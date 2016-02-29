# apache-log-parser

Parse an apache server log in Clojure.

[![Build Status](https://travis-ci.org/CaseyAMeakin/apache-log-parser.svg?branch=master)](https://travis-ci.org/CaseyAMeakin/apache-log-parser)


#### Usage

For Leiningen, include `[apache-log-parser "0.1.0"]` in your project.clj dependencies.  (See <a href="https://clojars.org/apache-log-parser">Clojars project page</a> for Maven and Gradle usage.)

You first need to define the format string which was used by Apache. This can be found in your `/etc/apache2/apache2.conf` file with keyword `LogFormat`.  Here's a simple example where I set up a standard default format:

```
(def log-format "%v:%p %h %l %u %t \"%r\" %>s %O \"%{Referer}i\" \"%{User-Agent}i\"")
```

You can read about these format codes in the Apache docs <a href="http://httpd.apache.org/docs/2.2/mod/mod_log_config.html">here</a>.

Having defined your log format, you can now parse a log file with the `parse-log-file` function
which takes a filename and the format string described above as its two input arguments `(parse-log-file log-filename log-format)`. Here's an example where I parse a log-file named "other\_vhosts\_access.log" and assign the output (which is a vector of hash-maps) with the variable `my-log`:

```
(def my-log (parse-log-file "other_vhosts_access.log" log-format))
```

Take a look at the tests for a few examples.

#### Example

Sample log

	static.movietime.co:80 66.249.79.99 - - [14/Feb/2016:14:04:44 +0000] "GET / HTTP/1.1" 403 493 "-" "Mozilla/5.0 (iPhone; CPU iPhone OS 8_3 like Mac OS X) AppleWebKit/600.1.4 (KHTML, like Gecko) Version/8.0 Mobile/12F70 Safari/600.1.4 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"


Program

```
(ns testing.core
  (:require [apache-log-parser.core :as alp])
  (:gen-class))
  
(defn -main
  "Usage example for apache-log-parser"
  [& args]
  (def log-format "%v:%p %h %l %u %t \"%r\" %>s %O \"%{Referer}i\" \"%{User-Agent}i\"")
  (def my-log (alp/parse-log-file "sample.log" log-format))
  (doseq [x (keys (first my-log))]
    (println x (x (first my-log))))
  )
```

Output  
  
```
$ lein run
:request-header-User-Agent Mozilla/5.0 (iPhone; CPU iPhone OS 8_3 like Mac OS X) AppleWebKit/600.1.4 (KHTML, like Gecko) Version/8.0 Mobile/12F70 Safari/600.1.4 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)
:remote-user -
:remote-host 66.249.79.99
:time [14/Feb/2016:14:04:44 +0000]
:server-port 80
:req-first-line GET / HTTP/1.1
:bytes-sent 493
:status 403
:request-header-Referer -
:server-name static.movietime.co
:remote-logname -
```


#### Acknowledgements

Inspired by the Python code <a href="https://github.com/rory/apache-log-parser">https://github.com/rory/apache-log-parser</a>.

#### Wish List

It would be nice to have better <b>date and time</b> handling and <b>user agent parsing.</b> User agent parsing with <a href="https://github.com/pingles/clj-detector">https://github.com/pingles/clj-detector</a> works well, but I didn't include it here to keep things dependency free.

#### License

nil

