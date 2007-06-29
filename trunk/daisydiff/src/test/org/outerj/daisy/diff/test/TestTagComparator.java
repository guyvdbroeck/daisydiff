package org.outerj.daisy.diff.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import org.outerj.daisy.diff.DiffFileWriter;
import org.outerj.daisy.diff.lcs.tag.Atom;
import org.outerj.daisy.diff.lcs.tag.TagComparator;

public class TestTagComparator {

	public static void main(String[] args) throws Exception {
		new TestTagComparator();
	}
	
	
	public TestTagComparator() throws Exception{
		StringBuilder leftsb = new StringBuilder();
//        leftsb.append(readResource("smallchange1.txt"));
		leftsb.append(readResource("daisymain.txt"));

		
        StringBuilder rightsb = new StringBuilder();
//        rightsb.append(readResource("smallchange2.txt"));
        rightsb.append(readResource("daisymainchanged.txt"));
        
        TagComparator lefttc = new TagComparator(leftsb);
        TagComparator righttc = new TagComparator(rightsb);
        
        for(Atom atom : lefttc.getAtoms()){
        	System.out.println(atom);
        }
        
        DiffFileWriter.diff("/home/guy/Desktop/difftag1.html", lefttc, righttc);
    	
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
}
