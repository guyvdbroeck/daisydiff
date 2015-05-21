# Daisy Diff offers two modes of operation #

Daisy Diff is actually two libraries in one! By default the HTML/Tree mode
is used. With an extra command line option the Tag mode can also be activated.

These modes are completely independent so you might find some cases when one
works better than the other.

## HTML mode ##

The Html mode is the default mode, and the one that will be used by most users. It performs
heavy pre-processing on the input html (cleaning it up) and actually creates an internal
tree that matches the input HTML.

The output of the HTML mode is what you would expect from an HTML diff tool. You
get the original document with annotations that show what has changed:

![http://daisydiff.googlecode.com/svn/wiki/images/daisyDiffHtmlMode.png](http://daisydiff.googlecode.com/svn/wiki/images/daisyDiffHtmlMode.png)

The main advantage of the HTML mode is that since it _parses_ the input it is very
smart when it comes to HTML tags. It understands the structure of the tags and can even
deal with changes in HTML attributes.

The disadvantage is that you might lost some information such as spacing from the input
text. Also sometimes the HTML mode is _too smart_ and produces results that are not
acceptable by human users.

HTML modes suffers from several issues regarding lists and tables.

## Tag mode ##

The Tag mode is a bit low level. It does not parse the input in any way. It treats everything as text. The only intelligence it has is the fact that it understands what text is part of a tag and what is not. But other than that, it cannot understand the tree structure of HTML.

For example if you add a bold style to a word, the HTML mode will report correctly that this word is now in bold. The tag mode will just report the addition of `<b>` and `</b>` tags as text changes without knowing that they constitute a tag pair. And of course
the user is not interested in the tags themselves, but the fact that the content between them has changed format.

It should be obvious from the above that Tag mode does not understand formatting changes
but only additions and removals.

The default Tag mode presentation escapes HTML tags as bellow:

![http://daisydiff.googlecode.com/svn/wiki/images/daisyDiffTagMode.png](http://daisydiff.googlecode.com/svn/wiki/images/daisyDiffTagMode.png)

The main advantage of the Tag mode is accuracy. It catches everything and even non-visible changes. For example HTML mode cannot always cope with changes in `colwidth` or `br` tags.

The tag mode is also perfect if you wish to develop your own diff library on top of Daisy Diff.

The main disadvantage of Tag mode is simplicity. There is no explicit support for formatting changes, changes in images and changes in HTML attributes. You have to implement them yourself if you want to post-process them in some way.

## Output formats ##

Another command line option can be used to change the output format of each mode.
The default format of both modes is HTML which is suitable for presentation to the user.

If you want to post-process the results or use them as input in your own software module,
both modes offer XML output which can be parsed with any XML parser.
