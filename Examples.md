# DaisyCMS example #
Daisy Diff is used in Daisy CMS for their compare page.
## Result ##
[Compare page](http://cocoondev.org/daisy/index/version/33/diff?&otherDocumentId=2-cd&otherBranch=main&otherLanguage=en&otherVersion=37&contentDiffType=html)
## Input ##
[old HTML](http://cocoondev.org/daisy/index/version/33) - [new HTML](http://cocoondev.org/daisy/index/version/37)
## Command ##
N/A (runs DaisyDiff embedded)


# BBC News example #
This example uses the BBC news archive site to compare the news page at different times on a given day.
The page can load slowly due to the slow servers at archive.org.
The page layout is somewhat shifted in the example because the page is not hosted on the BBC servers.
## Result ##
example.htm (link not available anymore)
## Input ##
[old HTML](http://web.archive.org/web/20070107145418/http://news.bbc.co.uk/) - [new HTML](http://web.archive.org/web/20070107182640/http://news.bbc.co.uk/)
## Command ##
```
java -jar daisydiff.jar http://web.archive.org/web/20070107145418/http://news.bbc.co.uk/
http://web.archive.org/web/20070107182640/http://news.bbc.co.uk/
--css=http://web.archive.org/web/20070107145418/http://news.bbc.co.uk/nol/shared/css/news_r5.css
```

# Diffing local HTML files #
## Command ##
```
java -jar daisydiff.jar file:///c:/sites/old.html file:///c:/sites/new.html
```








