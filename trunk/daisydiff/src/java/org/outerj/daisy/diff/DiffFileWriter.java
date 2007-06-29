package org.outerj.daisy.diff;

import java.io.File;

import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.helpers.AttributesImpl;

public class DiffFileWriter {

	public static void diff(String file, BlockComparator leftComparator, BlockComparator rightComparator) throws Exception {
		StreamResult streamResult = new StreamResult(
				new File(file));
		SAXTransformerFactory tf = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
		TransformerHandler serializer = tf.newTransformerHandler();
		
		serializer.setResult(streamResult);
		
		serializer.startDocument();
		AttributesImpl attrs = new AttributesImpl();
        serializer.startElement("", "html", "html", attrs);
        serializer.startElement("", "head", "head", attrs);
        serializer.endElement("", "head", "head");
        serializer.startElement("", "body", "body", attrs);
		
		DiffMarkup dm=new DiffMarkup(serializer);
		dm.printInfo();
		
		BlockDiffParser bdp = new BlockDiffParser(dm);
		
		Differ differ = new Differ(bdp);
		differ.blockDiff(leftComparator, rightComparator);
		
		serializer.endElement("", "body", "body");
        serializer.endElement("", "html", "html");
        serializer.endDocument();
	}
	
}
