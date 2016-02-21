(ns apache-log-parser.core-test
  (:require [clojure.test :refer :all]
            [apache-log-parser.core :as alp]))

(deftest base-test
  (def log-format "%h <<%P>> %t %Dus \"%r\" %>s %b  \"%{Referer}i\" \"%{User-Agent}i\" %l %u")
  (def sample-log-line "127.0.0.1 <<6113>> [16/Aug/2013:15:45:34 +0000] 1966093us \"GET / HTTP/1.1\" 200 3478  \"https://example.com/\" \"Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.18)\" - -")
  (def my-re (alp/make-logline-re log-format))
  (def log-keys (alp/format-keywords log-format))
  (def output (alp/parse-log-line sample-log-line my-re log-keys))
  (is (= (:status output) "200"))
  (is (= (:pid output) "6113"))
  (is (= (:req-first-line output) "GET / HTTP/1.1"))
  (is (= (:request-header-User-Agent output) "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.18)"))
  (is (= (:request-header-Referer output) "https://example.com/"))
  (is (= (apply hash-set (keys output)
                (hash-set :remote-host :pid :time :time-us :req-first-line :status :response-bytes-clf
                          :response-header-Referer :reseponse-header-User-Agent :remote-logname :remote-user)))))

(deftest remote-ip
  (def log-format "%a %l %u %t \"%r\" %>s %b")
  ;;(def log-format "%h %l %u %t \"%r\" %>s %b")
  (def sample-log-line "2001:0db8:85a3:0000:0000:8a2e:0370:7334 - frank [10/Oct/2000:13:55:36 -0700] \"GET /apache_pb.gif HTTP/1.0\" 200 2326")
  (def my-re (alp/make-logline-re log-format))
  (def log-keys (alp/format-keywords log-format))
  (def output (alp/parse-log-line sample-log-line my-re log-keys))
  (is (= (:log-data output) nil))
  (is (= (:remote-ip output) "2001:0db8:85a3:0000:0000:8a2e:0370:7334"))
  )

(deftest hostname-lookup
  (def log-format "%h %l %u %t \"%r\" %>s %b")
  (def sample-log-line "2001:0db8:85a3:0000:0000:8a2e:0370:7334 - frank [10/Oct/2000:13:55:36 -0700] \"GET /apache_pb.gif HTTP/1.0\" 200 2326")
  (def my-re (alp/make-logline-re log-format))
  (def log-keys (alp/format-keywords log-format))
  (def output (alp/parse-log-line sample-log-line my-re log-keys))
  (is (= (:log-data output) nil))
  (is (= (:remote-host output) "2001:0db8:85a3:0000:0000:8a2e:0370:7334"))
  )

(deftest null-status
  (def log-format "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\"")
  (def sample-log-line "002:52ee:xxxx::x - - [11/Jun/2014:22:55:45 +0000] \"GET /X230_2.51_g2uj10us.iso HTTP/1.1\" - 3414853 \"refer\" \"Mozilla/5.0 (X11; Linux x86_64; rv:29.0) Gecko/20100101 Firefox/29.0\"")
  (def my-re (alp/make-logline-re log-format))
  (def log-keys (alp/format-keywords log-format))
  (def output (alp/parse-log-line sample-log-line my-re log-keys))
  (is (= (:status output) "-"))
  )
      

