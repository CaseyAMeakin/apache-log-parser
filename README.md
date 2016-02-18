# apache-log-parser

Parse an apache server log.

#### Usage


First, define the format string used by Apache.  Look for keyword LogFormat in /etc/apache2/apache2.conf.  See: http://httpd.apache.org/docs/2.2/mod/mod_log_config.html

```
(def log-format "%v:%p %h %l %u %t \"%r\" %>s %O \"%{Referer}i\" \"%{User-Agent}i\"")
```

The log file can then be parsed using the `parse-log-file` function,
which takes a filename and the format string described above as its two input arguments.

```
(def my-log (parse-log-file "other_vhosts_access.log" log-format))
```

#### Shortcomings

1. Not all the format codes available have been programmed.
2. Needs some test code.

#### License

Nope


