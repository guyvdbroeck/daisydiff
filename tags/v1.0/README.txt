                              Daisy Diff 0.1

        This is Daisy Diff, the open source html diffing library!

               Website - http://code.google.com/p/daisydiff/
    
    +---------------------------------------------------------------+
    |                         STAND-ALONE                           |
    +---------------------------------------------------------------+
    
Java:
=====
java -jar daisydiff.jar [oldHTML] [newHTML] [optional arguments]


Optional Arguments:
===================
--file=[filename] - Write output to the specified file.
--type=[html/tag] - Use the html (default) diff algorithm or the tag diff.
--css=[cssfile1;cssfile2;cssfile3] - Add external CSS files.
--output=[html/xml] - Write html (default) or xml output.

Example:
========
java -jar daisydiff.jar http://web.archive.org/web/20070107145418/http://news.bbc.co.uk/ http://web.archive.org/web/20070107182640/http://news.bbc.co.uk/ --css=http://web.archive.org/web/20070107145418/http://news.bbc.co.uk/nol/shared/css/news_r5.css

java -jar daisydiff.jar http://cocoondev.org/wiki/291-cd/version/15/part/SimpleDocumentContent/data http://cocoondev.org/wiki/291-cd/version/22/part/SimpleDocumentContent/data --css=http://cocoondev.org/resources/skins/daisysite/css/daisy.css --output=xml --file=daisysite.htm

Requirements:
=============
Java 1.5 or 6

    +---------------------------------------------------------------+
    |                           EMBEDDED                            |
    +---------------------------------------------------------------+
    
Java:
=====
org.outerj.daisy.diff.DaisyDiff{

/**
 * Diffs two html files, outputting the result to the specified consumer.
 */
public static void diffHTML(InputSource oldSource, InputSource newSource, ContentHandler consumer, String prefix, Locale locale) throws SAXException, IOException;

/**
 * Diffs two html files word for word as source, outputting the result to
 * the specified consumer.
 */            
public static void diffTag(String oldText, String newText, ContentHandler consumer) throws Exception;

}

Requirements:
=============
Java 1.5 or 6
To run Daisy Diff embedded in your application, you don't need the entire Jar file. A much smaller Jar file without Xerces and NekoHtml will suffice.

