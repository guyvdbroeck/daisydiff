/*
 * Copyright 2004 Guy Van den Broeck
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.outerj.daisy.diff;

import java.io.IOException;
import java.io.StringReader;
import java.util.Locale;

import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import org.outerj.daisy.diff.html.HTMLDiffer;
import org.outerj.daisy.diff.html.HtmlSaxDiffOutput;
import org.outerj.daisy.diff.html.TextNodeComparator;
import org.outerj.daisy.diff.html.dom.DomTreeBuilder;
import org.outerj.daisy.diff.tag.TagComparator;
import org.outerj.daisy.diff.tag.TagDiffer;
import org.outerj.daisy.diff.tag.TagSaxDiffOutput;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class Diff {
    
    /**
     * Diffs two html files, outputting the result to the specified consumer.
     */
    public static void diffHTML(String text1, String text2, ContentHandler consumer, String prefix, Locale locale) throws SAXException, IOException{
        DomTreeBuilder leftHandler=new DomTreeBuilder();
    	XMLReader xr1 = XMLReaderFactory.createXMLReader();
        xr1.setContentHandler(leftHandler);
        xr1.setErrorHandler(leftHandler);
        xr1.parse(new InputSource(new StringReader(text1)));
        TextNodeComparator leftComparator = new TextNodeComparator(leftHandler, locale);
        
        
        DomTreeBuilder rightHandler=new DomTreeBuilder();
    	XMLReader xr2 = XMLReaderFactory.createXMLReader();
        xr2.setContentHandler(rightHandler);
        xr2.setErrorHandler(rightHandler);
        xr2.parse(new InputSource(new StringReader(text2)));
        TextNodeComparator rightComparator = new TextNodeComparator(rightHandler, locale);
        
        HtmlSaxDiffOutput output = new HtmlSaxDiffOutput(consumer, prefix);
        HTMLDiffer differ = new HTMLDiffer(output);
        differ.diff(leftComparator, rightComparator);
    }
    
    /**
     * Diffs two html files word for word as source, outputting the result 
     * to the specified consumer.
     */
    public static void diffTag(String text1, String text2, ContentHandler consumer) throws Exception{
        TagComparator left=new TagComparator(text1);
        TagComparator right=new TagComparator(text2);
        
        TagSaxDiffOutput output = new TagSaxDiffOutput(consumer);
        TagDiffer differ=new TagDiffer(output);
        differ.diff(left, right);
    }

}
