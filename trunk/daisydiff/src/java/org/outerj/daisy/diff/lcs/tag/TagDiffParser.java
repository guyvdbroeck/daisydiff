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
package org.outerj.daisy.diff.lcs.tag;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.compare.rangedifferencer.PublicRangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import org.outerj.daisy.diff.MarkupGenerator;
import org.outerj.daisy.diff.lcs.rangecomparator.IAtomSplitter;
import org.outerj.daisy.diff.lcs.rangecomparator.TagComparator;

public class TagDiffParser {

	private MarkupGenerator markup;


	public TagDiffParser(MarkupGenerator markup){
		this.markup=markup;
	}
	
	public void parseNoChange(int beginLeft, int endLeft,
			int beginRight, int endRight, 
			IAtomSplitter leftComparator, IAtomSplitter rightComparator) throws Exception{
		
		StringBuilder sb = new StringBuilder();
		
		while(beginLeft<endLeft){
			
			while(beginLeft<endLeft && !rightComparator.getAtom(beginRight).hasInternalIdentifiers()
				&& !leftComparator.getAtom(beginLeft).hasInternalIdentifiers()){
				sb.append(rightComparator.getAtom(beginRight).getFullText());
				beginRight++; beginLeft++;
			}
			
			if(sb.length()>0){
				markup.addClearPart(sb.toString());
				sb.setLength(0);
			}
			
			if (beginLeft<endLeft) {
				
				IAtomSplitter leftComparator2 = new ArgumentComparator(
						leftComparator.getAtom(beginLeft).getFullText());
				IAtomSplitter rightComparator2 = new ArgumentComparator(
						rightComparator.getAtom(beginRight)
								.getFullText());
				
				RangeDifference[] differences2 = RangeDifferencer
						.findDifferences(leftComparator2,
								rightComparator2);
				List<PublicRangeDifference> pdifferences2 = preProcess(
						differences2, 1);
				
				int rightAtom2 = 0;
				for (int j = 0; j < pdifferences2.size(); j++) {
					if (rightAtom2 < pdifferences2.get(j).rightStart()){
						markup.addClearPart(rightComparator2.substring(
								rightAtom2, pdifferences2.get(j)
										.rightStart()));
					}
					if (pdifferences2.get(j).leftLength() > 0){
						markup.addRemovedPart(leftComparator2
								.substring(pdifferences2.get(j)
										.leftStart(), pdifferences2
										.get(j).leftEnd()));
					}
	
					if (pdifferences2.get(j).leftLength() > 0
							&& pdifferences2.get(j).rightLength() > 0){
						markup.addSeperator("»");
					}
	
					if (pdifferences2.get(j).rightLength() > 0){
						markup.addAddedPart(rightComparator2.substring(
								pdifferences2.get(j).rightStart(),
								pdifferences2.get(j).rightEnd()));
					}
					
					rightAtom2 = pdifferences2.get(j).rightEnd();
	
				}
				if (rightAtom2 < rightComparator2.getRangeCount())
					markup.addClearPart(rightComparator2
							.substring(rightAtom2));
				beginLeft++; beginRight++;
			}			
		
		}

	}
	
	public void parseNewDiff(IAtomSplitter leftComparator, IAtomSplitter rightComparator) throws Exception {
		
		RangeDifference[] differences = RangeDifferencer.findDifferences(leftComparator, rightComparator);
	    
		List<PublicRangeDifference> pdifferences = preProcess(differences,leftComparator);
		
		int rightAtom=0;
		int leftAtom=0;
		
		for(int i=0;i<pdifferences.size();i++){
        	
			parseNoChange(leftAtom, pdifferences.get(i).leftStart(),
					rightAtom, pdifferences.get(i).rightStart(),
					leftComparator, rightComparator);
				
        	if(pdifferences.get(i).leftLength()>0)
        		markup.addRemovedPart(leftComparator.substring(pdifferences.get(i).leftStart(),pdifferences.get(i).leftEnd()));
        	
        	if(pdifferences.get(i).leftLength()>0 && pdifferences.get(i).rightLength()>0)
        		markup.addSeperator("|");
        	        	
        	if(pdifferences.get(i).rightLength()>0)
        		markup.addAddedPart(rightComparator.substring(pdifferences.get(i).rightStart(), pdifferences.get(i).rightEnd()));
        	
        	rightAtom=pdifferences.get(i).rightEnd();
        	leftAtom=pdifferences.get(i).leftEnd();
			
        }
		if(rightAtom<rightComparator.getRangeCount())
			parseNoChange(leftAtom, pdifferences.get(pdifferences.size()-1).leftEnd(),
					rightAtom, pdifferences.get(pdifferences.size()-1).rightEnd(), 
					leftComparator, rightComparator);
        
		
	}

	private List<PublicRangeDifference> preProcess(RangeDifference[] differences
			, IAtomSplitter leftComparator) {
		
		List<PublicRangeDifference> newRanges = new LinkedList<PublicRangeDifference>();
		
		for(int i=0;i<differences.length;i++){
			
			int leftStart = differences[i].leftStart();
			int leftEnd = differences[i].leftEnd();
			int rightStart = differences[i].rightStart();
			int rightEnd = differences[i].rightEnd();
			int kind = differences[i].kind();
			int temp = leftEnd;
			boolean connecting = true;
			
			
			while(connecting 
					&& i+1<differences.length && differences[i+1].kind()==kind){
				
				int bridgelength=Math.min((leftEnd-leftStart)*2-2,10);
				
				System.out.println("bridgelength= "+bridgelength);
				while((leftComparator.getAtom(temp) instanceof DelimiterAtom
						|| (bridgelength-->0)
						)
						&& temp<=differences[i+1].leftStart()
						){
					//bridgelength--;
					System.out.println("bridgelength= "+bridgelength);
					
					temp++;
					
				}
				if(temp==differences[i+1].leftStart()){
					leftEnd = differences[i+1].leftEnd();
					rightEnd = differences[i+1].rightEnd();
					temp = leftEnd;
					i++;
				}else{
					System.out.println("bridge stopped at token "+leftComparator.getAtom(temp).getFullText());
					connecting = false;
				}
			}
			newRanges.add(new PublicRangeDifference(kind, rightStart, rightEnd-rightStart
					, leftStart, leftEnd-leftStart));
		}
		
		
		return newRanges;
	}

	private List<PublicRangeDifference> preProcess(RangeDifference[] differences
			, int span) {
		
		List<PublicRangeDifference> newRanges = new LinkedList<PublicRangeDifference>();
		
		for(int i=0;i<differences.length;i++){
			
			int leftStart = differences[i].leftStart();
			int leftEnd = differences[i].leftEnd();
			int rightStart = differences[i].rightStart();
			int rightEnd = differences[i].rightEnd();
			int kind = differences[i].kind();
			
			while(i+1<differences.length && differences[i+1].kind()==kind
					&& differences[i+1].leftStart()<=leftEnd+span
					&& differences[i+1].rightStart()<=rightEnd+span
					){
				leftEnd = differences[i+1].leftEnd();
				rightEnd = differences[i+1].rightEnd();
				i++;
			}
			
			newRanges.add(new PublicRangeDifference(kind, rightStart, rightEnd-rightStart
					, leftStart, leftEnd-leftStart));
		}
		
		return newRanges;
	}

}
