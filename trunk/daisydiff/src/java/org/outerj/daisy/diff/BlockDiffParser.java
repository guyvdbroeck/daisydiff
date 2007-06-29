package org.outerj.daisy.diff;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.compare.rangedifferencer.RangeDifference;

public class BlockDiffParser {

	private DiffMarkup markup;


	public BlockDiffParser(DiffMarkup markup){
		this.markup=markup;
	}
	
	
	public void parseNewDiff(BlockComparator leftComparator, BlockComparator rightComparator, RangeDifference[] differences) throws Exception {
		
		List<PublicRangeDifference> pdifferences = preProcess(differences,leftComparator);
		
		int line=0;
        
		for(int i=0;i<pdifferences.size();i++){
        	
			int start = line; //obsolete
        	
			markup.addClearPart(rightComparator.substring(start, pdifferences.get(i).rightStart()));
        	
        	if(pdifferences.get(i).leftLength()>0)
        		markup.addRemovedPart(leftComparator.substring(pdifferences.get(i).leftStart(),pdifferences.get(i).leftEnd()));
        	
        	if(pdifferences.get(i).leftLength()>0 && pdifferences.get(i).rightLength()>0)
        		markup.addSeperator();
        	        	
        	if(pdifferences.get(i).rightLength()>0)
        		markup.addAddedPart(rightComparator.substring(pdifferences.get(i).rightStart(), pdifferences.get(i).rightEnd()));
        	
        	line=pdifferences.get(i).rightEnd();
        }
		
		int start = line; //obsolete
    	
		markup.addClearPart(rightComparator.substring(start));
        
		
	}


	private List<PublicRangeDifference> preProcess(RangeDifference[] differences, BlockComparator leftComparator) {
		List<PublicRangeDifference> newRanges = new LinkedList<PublicRangeDifference>();
		
		for(int i=0;i<differences.length;i++){
			
			int leftStart = differences[i].leftStart();
			int leftEnd = differences[i].leftEnd();
			int rightStart = differences[i].rightStart();
			int rightEnd = differences[i].rightEnd();
			int kind = differences[i].kind();
			
			if(i+1<differences.length){
				while(differences[i+1].kind()==kind
						&& differences[i+1].leftStart()<=leftEnd+2
						&& differences[i+1].rightStart()<=rightEnd+2
						&& differences[i+1].rightLength()>0
						&& differences[i+1].leftLength()>0
						
//						&& (leftComparator.getToken(leftEnd+1).equals(" ")
//								|| leftComparator.getToken(leftEnd+1).equals(".")
//								|| leftComparator.getToken(leftEnd+1).equals("!")
//								|| leftComparator.getToken(leftEnd+1).equals(",")
//								|| leftComparator.getToken(leftEnd+1).equals(";")
//								|| leftComparator.getToken(leftEnd+1).equals("?")
//								|| leftComparator.getToken(leftEnd+1).equals(" ")
//								|| leftComparator.getToken(leftEnd+1).equals("=")
//								|| leftComparator.getToken(leftEnd+1).equals("\\")
//								|| leftComparator.getToken(leftEnd+1).equals("\"")
//								|| leftComparator.getToken(leftEnd+1).equals("\t")
//								|| leftComparator.getToken(leftEnd+1).equals("\r")
//								|| leftComparator.getToken(leftEnd+1).equals("\n"))
						){
					leftEnd = differences[i+1].leftEnd();
					rightEnd = differences[i+1].rightEnd();
					i++;
				}
			}
			newRanges.add(new PublicRangeDifference(kind, rightStart, rightEnd-rightStart
					, leftStart, leftEnd-leftStart));
		}
		
		return newRanges;
	}

}
