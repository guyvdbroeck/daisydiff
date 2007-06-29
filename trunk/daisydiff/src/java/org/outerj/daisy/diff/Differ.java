package org.outerj.daisy.diff;

import org.eclipse.compare.rangedifferencer.IRangeComparator;
import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;

public class Differ {
	
	private BlockDiffParser parser;
	
	public Differ(BlockDiffParser parser){
		this.parser=parser;
	}
	
	public void blockDiff(BlockComparator leftComparator, BlockComparator rightComparator) throws Exception{
		
		RangeDifference[] differences = RangeDifferencer.findDifferences(leftComparator, rightComparator);
	    
		parser.parseNewDiff(leftComparator, rightComparator, differences);
	}
	
}
