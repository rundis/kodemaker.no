(ns kodemaker-no.formatting-test
  (:require [kodemaker-no.formatting :refer :all]
            [midje.sweet :refer :all]))

(fact "We don't want any short typographic widows, but long ones are fine."
      (no-widows "Hello there, how are you?") => "Hello there, how are&nbsp;you?"
      (no-widows "Hello there, are you Slartibartfast?") => "Hello there, are you Slartibartfast?")

(fact "We process markdown."
      (to-html "# hi\nhow are you") => "<h1>hi</h1>\n<p>how are you</p>\n"
      (to-html "```clojure\n(+ 3 3)\n```") => "<pre><code class=\"language-clojure\">(+ 3 3)\n</code></pre>\n")

(fact "We link to stuff."
      (to-html "Send mail til post@kodemaker.no da vel.")
      => "<p>Send mail til <a href=\"mailto:post@kodemaker.no\">post@kodemaker.no</a> da vel.</p>\n")

(fact "We strike through text."
      (to-html "Denne teksten er ~~strøket gjennom~~.")
      => "<p>Denne teksten er <del>strøket gjennom</del>.</p>\n")

(fact "We are friendly to markdown from edn-files, de-indenting the text properly."
      (to-html "The first line usually has no indentation,
                but then the second line has quite a lot.
                This normally isn't a problem for markdown.

                But when you add a second paragraph, things
                go to hell in a handbasket. This is suddenly a
                highly indented code block. Not what you wanted
                at all.

                The problem is of course increased further when you
                want an actual code block, like this:

                    Some code here

                So, we handle it. Neat, huh?")
      => (str "<p>The first line usually has no indentation,\nbut then the second line has quite a lot.\nThis normally isn&rsquo;t a problem for markdown.</p>\n"
              "<p>But when you add a second paragraph, things\ngo to hell in a handbasket. This is suddenly a\nhighly indented code block. Not what you wanted\nat all.</p>\n"
              "<p>The problem is of course increased further when you\nwant an actual code block, like this:</p>"
              "\n<pre><code>Some code here\n</code></pre>\n"
              "<p>So, we handle it. Neat, huh?</p>\n"))

(fact (comma-separated ["1"]) => ["1"]
      (comma-separated ["1" "2"]) => ["1" " og " "2"]
      (comma-separated ["1" "2" "3"]) => ["1" ", " "2" " og " "3"]
      (comma-separated ["1" "2" "3" "4"]) => ["1" ", " "2" ", " "3" " og " "4"])

(fact (year-range [2009]) => "2009")
(fact (year-range [2009 :ongoing]) => "2009-")
(fact (year-range [2009 2010]) => "2009-2010")
(fact (year-range [2009 2010 2011]) => "2009-2011")
(fact (year-range [2009 2011]) => "2009, 2011")
(fact (year-range [2009 2010 2013]) => "2009-2010, 2013")
