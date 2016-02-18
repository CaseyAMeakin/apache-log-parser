# apache-log-parser

Parse an apache server log.

#### Usage


<p>First, define the format string used by Apache.  Look for keyword LogFormat in /etc/apache2/apache2.conf.  See: http://httpd.apache.org/docs/2.2/mod/mod_log_config.html</p>

<p><pre><code>(def log-format "%v:%p %h %l %u %t \"%r\" %>s %O \"%{Referer}i\" \"%{User-Agent}i\"")<code></pre></p>

<p>The log file can then be parsed using the <code>parse-log-file</code> function,
which takes a filename and the format string described above as its two input arguments.</p>

<p><pre></code>(def my-log (parse-log-file "other_vhosts_access.log" log-format))</code></pre></p>

#### Shortcomings

Not all the format codes available have been programmed.  Needs some test code.

#### License

Nope


