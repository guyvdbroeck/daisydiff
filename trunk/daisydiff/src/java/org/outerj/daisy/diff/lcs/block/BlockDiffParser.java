/*
 * Copyright 2007 Outerthought bvba and Schaubroeck nv
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
package org.outerj.daisy.diff.lcs.block;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.compare.rangedifferencer.PublicRangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.outerj.daisy.diff.MarkupGenerator;

public class BlockDiffParser {

	private MarkupGenerator markup;


	public BlockDiffParser(MarkupGenerator markup){
		this.markup=markup;
	}
	
	
	public void parseNewDiff(BlockComparator leftComparator, BlockComparator rightComparator, RangeDifference[] differences) throws Exception {
		
		List<PublicRangeDifference> pdifferences = preProcess(differences,leftComparator);
		
		int line=0;
        
		for(int i=0;i<pdifferences.size();i++){
        	
			int start = line;//(line>0)?line-1:0; //obsolete
        	
			markup.addClearPart(rightComparator.substring(start, pdifferences.get(i).rightStart()));
        	
        	if(pdifferences.get(i).leftLength()>0)
        		markup.addRemovedPart(leftComparator.substring(pdifferences.get(i).leftStart(),pdifferences.get(i).leftEnd()));
        	
        	if(pdifferences.get(i).leftLength()>0 && pdifferences.get(i).rightLength()>0)
        		markup.addSeperator("|");
        	        	
        	if(pdifferences.get(i).rightLength()>0)
        		markup.addAddedPart(rightComparator.substring(pdifferences.get(i).rightStart(), pdifferences.get(i).rightEnd()));
        	
        	line=pdifferences.get(i).rightEnd();
        }
		
		int start = line;//(line>0)?line-1:0; //obsolete
    	
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
						&& differences[i+1].leftStart()<=leftEnd+1
						&& differences[i+1].rightStart()<=rightEnd+1
						&& differences[i+1].rightLength()>0
						&& differences[i+1].leftLength()>0
						
//						&& (leftComparator.getToken(leftEnd+1).equals(" ")
//								|| leftComparator.getToken(leftEnd).equals(".")
//								|| leftComparator.getToken(leftEnd).equals("!")
//								|| leftComparator.getToken(leftEnd).equals(",")
//								|| leftComparator.getToken(leftEnd).equals(";")
//								|| leftComparator.getToken(leftEnd).equals("?")
//								|| leftComparator.getToken(leftEnd).equals(" ")
//								|| leftComparator.getToken(leftEnd).equals("=")
//								|| leftComparator.getToken(leftEnd).equals("\\")
//								|| leftComparator.getToken(leftEnd).equals("\"")
//								|| leftComparator.getToken(leftEnd).equals("\t")
//								|| leftComparator.getToken(leftEnd).equals("\r")
//								|| leftComparator.getToken(leftEnd).equals("\n"))
						){
					leftEnd = differences[i+1].leftEnd();
					rightEnd = differences[i+1].rightEnd();
					i++;
					System.out.println(leftComparator.getToken(leftEnd));
				}
			}
			newRanges.add(new PublicRangeDifference(kind, rightStart, rightEnd-rightStart
					, leftStart, leftEnd-leftStart));
		}
		
		return newRanges;
	}

}
