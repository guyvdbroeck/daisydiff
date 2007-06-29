package org.outerj.daisy.diff.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.outerj.daisy.diff.DiffFileWriter;
import org.outerj.daisy.diff.lcs.block.BlockComparator;

public class TestBlockDiff {

	public static void main(String[] args) throws Exception {
		new TestBlockDiff();
	}
	
	
	public TestBlockDiff() throws Exception{
		StringBuilder leftsb = new StringBuilder();
//        leftsb.append(readResource("smallchange1.txt"));
		leftsb.append(readResource("daisymain.txt"));

		
        StringBuilder rightsb = new StringBuilder();
//        rightsb.append(readResource("smallchange2.txt"));
        rightsb.append(readResource("daisymainchanged.txt"));
        
        BlockComparator leftComparator = new BlockComparator(leftsb);
        BlockComparator rightComparator = new BlockComparator(rightsb);
        
        
        DiffFileWriter.diff("/home/guy/Desktop/diff2.html", leftComparator, rightComparator);
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
