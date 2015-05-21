# Introduction #
Daisy Diff is a Java library that diffs (compares) HTML files. It highlights added and removed words and annotates changes to the styling. ([Examples](Examples.md))

This project was a [Google Summer of Code 2007](http://code.google.com/soc/2007/) project for [DaisyCMS](http://daisycms.org) where it's actively used for diffing HTML content.
As a spin-off, a [PHP version](http://www.mediawiki.org/wiki/Visual_Diff) of the algorithm was developed for [MediaWiki](http://www.mediawiki.org/) in the [GSoC 2008](http://code.google.com/soc/2007/).

The Java version is licensed under the Apache License v2. The PHP version is GPLv2+. Other licenses can be requested.

# Features #
  * Works with badly formed HTML that can be found "in the wild".
  * The diffing is more specialized in HTML than XML tree differs. Changing part of a text node will not cause the entire node to be changed.
  * In addition to the default visual diff, HTML source can be diffed coherently.
  * Provides easy to understand descriptions of the changes.
  * The default GUI allows easy browsing of the modifications through keyboard shortcuts and links.

# Download #

A stand-alone Java library is available in the [download section](http://code.google.com/p/daisydiff/downloads/list). To embed Daisy Diff in your application you can checkout [our Subversion repository](http://code.google.com/p/daisydiff/source) (The [Main class](http://code.google.com/p/daisydiff/source/browse/trunk/daisydiff/src/java/org/outerj/daisy/diff/Main.java) is a good starting point). The PHP implementation is available in the [MediaWiki repository](http://www.mediawiki.org/wiki/Subversion).

# Contact #
Questions about Daisy Diff or HTML diffing can be sent to our [developer mailing list](http://groups.google.com/group/daisydiff).