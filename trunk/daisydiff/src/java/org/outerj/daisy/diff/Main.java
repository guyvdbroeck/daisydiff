package org.outerj.daisy.diff;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.xml.sax.InputSource;
import org.xml.sax.helpers.AttributesImpl;

public class Main {

    public final static String cssPath = "css/diff.css";
    
    public static void main(String[] args) throws URISyntaxException {
        System.out.println("     ______________");
        System.out.println("    /Daisy Diff 0.1\\");
        System.out.println("   /________________\\");
        System.out.println("");
        System.out.println();
        if(args.length<2)
            help();

        boolean htmlDiff = true;
        boolean tidy = false;
        String outputFile = "daisydiff.htm";

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

            InputStream oldStream = new URI(args[0]).toURL().openStream();
            InputStream newStream = new URI(args[1]).toURL().openStream();


            if(htmlDiff){
                result.startDocument();
                Locale locale = Locale.getDefault();
                String prefix = "diff";
                
                HtmlCleaner cleaner = new HtmlCleaner();
                
                InputSource oldSource = new InputSource(oldStream);
                InputSource newSource = new InputSource(newStream);
                
                DomTreeBuilder oldHandler=new DomTreeBuilder();
                cleaner.clean(oldSource, oldHandler);
                TextNodeComparator leftComparator = new TextNodeComparator(oldHandler, locale);

                String[] cssPath = new String[]{Main.cssPath};
                String[] jsPath = new String []{"js/tooltip/wz_tooltip.js","js/tooltip/tip_balloon.js","js/dojo/dojo.js","js/diff.js"};
                
                DomTreeBuilderWithHeaders newHandler = new DomTreeBuilderWithHeaders(result, cssPath, jsPath);
                cleaner.clean(newSource, newHandler);
                TextNodeComparator rightComparator = new TextNodeComparator(newHandler, locale);

                HtmlSaxDiffOutput output = new HtmlSaxDiffOutput(result, prefix);
                HTMLDiffer differ = new HTMLDiffer(output);
                differ.diff(leftComparator, rightComparator);

                newHandler.writeClosingTag();
                
                result.endDocument();
            }else{
                result.startDocument();
                result.startElement("", "html", "html", new AttributesImpl());
                result.startElement("", "head", "head", new AttributesImpl());
                AttributesImpl attrs = new AttributesImpl();
                attrs.addAttribute("", "href", "href", "CDATA", cssPath);
                attrs.addAttribute("", "type", "type", "CDATA", "text/css");
                attrs.addAttribute("", "rel", "rel", "CDATA", "stylesheet");
                result.startElement("", "link", "link", attrs);
                result.endElement("", "link", "link");
                result.endElement("", "head", "head");
                result.startElement("", "body", "body", new AttributesImpl());

                DaisyDiff.diffTag(new BufferedReader(new InputStreamReader(oldStream))
                , new BufferedReader(new InputStreamReader(newStream)), result);
                result.endElement("", "body", "body");
                result.endElement("", "html", "html");
                result.endDocument();
            }

            result.endDocument();
        } catch (Throwable e) {
            e.printStackTrace();
            help();
        } 
        System.out.println("done");

    }

    private static void help() {
        System.out.println("==========================");
        System.out.println("DAISY DIFF HELP:");
        System.out.println("java -jar daisydiff.jar [oldHTML] [newHTML]");
        System.out.println("--file=[filename] - Write output to the specified file.");
        System.out.println("--type=[html/tag] - Use the html (default) diff algorithm or the tag diff.");
        System.out.println("examples: ");
        System.out.println("java -jar daisydiff.jar http://www.google.com http://www.froogle.com --tidy");
        System.out.println("==========================");
        System.exit(0);
    }
    
}
