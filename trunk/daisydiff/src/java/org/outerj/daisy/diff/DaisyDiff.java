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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.outerj.daisy.diff.html.HTMLDiffer;
import org.outerj.daisy.diff.html.HtmlSaxDiffOutput;
import org.outerj.daisy.diff.html.TextNodeComparator;
import org.outerj.daisy.diff.html.dom.DomTreeBuilder;
import org.outerj.daisy.diff.html.dom.DomTreeBuilderWithHeaders;
import org.outerj.daisy.diff.tag.TagComparator;
import org.outerj.daisy.diff.tag.TagDiffer;
import org.outerj.daisy.diff.tag.TagSaxDiffOutput;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class DaisyDiff {

    public final static String cssPath = "css/diff.css";

    /**
     * Diffs two html files, outputting the result to the specified consumer.
     */
    public static void diffHTML(InputSource oldSource, InputSource newSource, ContentHandler consumer, String prefix, Locale locale) throws SAXException, IOException{   
        consumer.startDocument();
        DomTreeBuilder oldHandler=new DomTreeBuilder();
        XMLReader xr1 = XMLReaderFactory.createXMLReader();
        xr1.setContentHandler(oldHandler);
        xr1.setErrorHandler(oldHandler);
        xr1.parse(oldSource);
        TextNodeComparator leftComparator = new TextNodeComparator(oldHandler, locale);

        String[] cssPath = new String[]{"css/diff.css"};
        String[] jsPath = new String []{"js/tooltip/wz_tooltip.js","js/tooltip/tip_balloon.js","js/dojo/dojo.js","js/diff.js"};
        DomTreeBuilderWithHeaders newHandler = new DomTreeBuilderWithHeaders(consumer, cssPath, jsPath);
        XMLReader xr2 = XMLReaderFactory.createXMLReader();
        xr2.setContentHandler(newHandler);
        xr2.setErrorHandler(newHandler);
        xr2.parse(newSource);
        TextNodeComparator rightComparator = new TextNodeComparator(newHandler, locale);

        HtmlSaxDiffOutput output = new HtmlSaxDiffOutput(consumer, prefix);
        HTMLDiffer differ = new HTMLDiffer(output);
        differ.diff(leftComparator, rightComparator);

        newHandler.writeClosingTag();
        consumer.endDocument();
    }

    /**
     * Diffs two html files word for word as source, outputting the result 
     * to the specified consumer.
     */
    public static void diffTag(String oldText, String newText, ContentHandler consumer) throws Exception{
        consumer.startDocument();
        TagComparator oldComp=new TagComparator(oldText);
        TagComparator newComp=new TagComparator(newText);

        TagSaxDiffOutput output = new TagSaxDiffOutput(consumer);
        TagDiffer differ=new TagDiffer(output);
        differ.diff(oldComp, newComp);
        consumer.endDocument();
    }

    /**
     * Diffs two html files word for word as source, outputting the result 
     * to the specified consumer.
     */
    public static void diffTag(BufferedReader oldText, BufferedReader newText, ContentHandler consumer) throws Exception{
        consumer.startDocument();
        TagComparator oldComp=new TagComparator(oldText);
        TagComparator newComp=new TagComparator(newText);

        TagSaxDiffOutput output = new TagSaxDiffOutput(consumer);
        TagDiffer differ=new TagDiffer(output);
        differ.diff(oldComp, newComp);
        consumer.endDocument();
    }

    public static void main(String[] args) throws URISyntaxException {
        System.out.println("     ______________");
        System.out.println("    /Daisy Diff 0.9\\");
        System.out.println("   /________________\\");
        System.out.println("");
        System.out.println();
        if(args.length<2)
            help();

        boolean htmlDiff = true;
        String outputFile = "diff.htm";

        try {
            for(int i=2;i<args.length;i++){
                String[] split =  args[i].split("=");
                if(split[0].equalsIgnoreCase("--file")){
                    outputFile = split[1];
                }else if(split[0].equalsIgnoreCase("--type")){
                    if(split[1].equalsIgnoreCase("tag")){
                        htmlDiff=false;
                    }
                }
            }

            System.out.println("Comparing documents:");
            System.out.println("  "+args[0]);
            System.out.println("and");
            System.out.println("  "+args[1]);
            System.out.println();
            if(htmlDiff)
                System.out.println("Diff type: html");
            else
                System.out.println("Diff type: tag");
            System.out.println("Writing output to: "+outputFile);
            System.out.println();
            SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory
            .newInstance();

            TransformerHandler result;
            result = tf.newTransformerHandler();
            result.setResult(new StreamResult(new File(outputFile)));
            result.startDocument();

            if(htmlDiff){
                InputSource oldSource = new InputSource(args[0]);
                InputSource newSource = new InputSource(args[1]);
                diffHTML(oldSource, newSource, result, "diff", Locale.ENGLISH);
            }else{
                URL oldSource = new URI(args[0]).toURL();
                URL newSource = new URI(args[1]).toURL();
                
                diffTag(new BufferedReader(new InputStreamReader(oldSource.openStream()))
                , new BufferedReader(new InputStreamReader(newSource.openStream())), result);
            }

            result.endDocument();
        } catch (Throwable e) {
            e.printStackTrace();
            help();
        } 
        System.out.println("done");

    }

    private static void help() {
        System.out.println("DAISY DIFF HELP:");
        System.out.println("java -jar daisydiff.jar [oldHTML] [newHTML]");
        System.out.println("--file=[filename] - Write output to the specified file.");
        System.out.println("--type=[html/tag] - Use the html (default) diff algorithm or the tag diff.");
        System.out.println("examples: ");
        System.out.println("java -jar daisydiff.jar http://www.google.com http://www.froogle.com");
        System.exit(0);
    }

}
