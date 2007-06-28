/*
 * Copyright 2004 Outerthought bvba and Schaubroeck nv
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
package org.outerj.daisy.diff.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import org.outerj.daisy.diff.BlockComparator;
import org.xml.sax.helpers.AttributesImpl;

public class LineTest {

	
	public void testDiff() throws Exception {
	    	
		StreamResult streamResult = new StreamResult(new File("/home/guy/Desktop/diff.html"));
		SAXTransformerFactory tf = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
		TransformerHandler serializer = tf.newTransformerHandler();
		serializer.setResult(streamResult);
		
		serializer.startDocument();
		AttributesImpl attrs = new AttributesImpl();
        serializer.startElement("", "html", "html", attrs);
        serializer.startElement("", "head", "head", attrs);
        serializer.endElement("", "head", "head");
        serializer.startElement("", "body", "body", attrs);
        
        MarkupTest mt = new MarkupTest(serializer);
        
        mt.printInfo();
        
        StringBuilder leftsb = new StringBuilder();
        leftsb.append(readResource("linetest.txt"));

        StringBuilder rightsb = new StringBuilder();
        rightsb.append(readResource("linetestchanged.txt"));
        
        BlockComparator leftComparator = new BlockComparator(leftsb);
        BlockComparator rightComparator = new BlockComparator(rightsb);

//        for(int i=0;i<90;i++){
//        	System.out.println(rightComparator.getToken(i));
//        }
        
        RangeDifference[] differences = RangeDifferencer.findDifferences(leftComparator, rightComparator);
       
        System.out.println(leftComparator.substring(0));
    	
        
        int line=0;
        for(RangeDifference difference : differences){
        	int start = (line>0)?line-1:0;
        	mt.addClearPart(leftComparator.substring(start, difference.leftStart()));
        	
        	
        	
        	if(difference.leftLength()>0)
        		mt.addRemovedPart(leftComparator.substring(difference.leftStart(), difference.leftEnd()));
        	//System.out.println(leftComparator.substring(difference.leftStart(), difference.leftEnd()));
        	
        	if(difference.leftLength()>0 && difference.rightLength()>0)
        		mt.addSeperator();
        	
        	
        	if(difference.rightLength()>0)
        		mt.addAddedPart(rightComparator.substring(difference.rightStart(), difference.rightEnd()));
            //System.out.println(rightComparator.substring(difference.rightStart(), difference.rightEnd()));
        	
        	line=difference.leftEnd()+1;
        }
//        mt.addClearPart(leftComparator.substring(line-1));
       
        serializer.endElement("", "body", "body");
        serializer.endElement("", "html", "html");
        serializer.endDocument();
    }


    String readResource(String name) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("org/outerj/daisy/diff/test/" + name);
        Reader reader = new InputStreamReader(is, "UTF-8");
        BufferedReader bufferedReader = new BufferedReader(reader);

        StringBuilder buffer = new StringBuilder();
        int c = bufferedReader.read();
        while (c != -1) {
            buffer.append((char)c);
            c = bufferedReader.read();
        }

        return buffer.toString();
    }
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		(new LineTest()).testDiff();
	}

}
