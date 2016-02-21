# apache-log-parser

Parse an apache server log in Clojure.

#### Usage


You first need to define the format string which was used by Apache. This can be found in your `/etc/apache2/apache2.conf` file with keyword `LogFormat`.  Here's a simple example where I set up a standard default format:

```
(def log-format "%v:%p %h %l %u %t \"%r\" %>s %O \"%{Referer}i\" \"%{User-Agent}i\"")
```

You can read about these format codes in the Apache docs at http://httpd.apache.org/docs/2.2/mod/mod\_log\_config.html.

Having defined your log format, you can now parse a log file with the `parse-log-file` function
which takes a filename and the format string described above as its two input arguments `(parse-log-file log-filename log-format)`. Here's an example where I parse a log-file named "other\_vhosts\_access.log" and assign the output (which is an array of hash-maps) with the variable `my-log`:

```
(def my-log (parse-log-file "other_vhosts_access.log" log-format))
```

Take a look at the tests for a few examples.

#### Acknowledgements

Inspired by the Python code <a href="https://github.com/rory/apache-log-parser">https://github.com/rory/apache-log-parser</a>.

#### Wish List

Would be nice to have better <b>date and time</b> handling and <b>user agent parsing.</b> User agent parsing with <a href="https://github.com/pingles/clj-detector">https://github.com/pingles/clj-detector</a> works well, but I didn't include it here to keep things dependency free.

#### License

nil

