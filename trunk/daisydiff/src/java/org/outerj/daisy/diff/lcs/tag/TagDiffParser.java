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

import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.outerj.daisy.diff.DiffMarkup;
import org.outerj.daisy.diff.PublicRangeDifference;

public class TagDiffParser {

	private DiffMarkup markup;


	public TagDiffParser(DiffMarkup markup){
		this.markup=markup;
	}
	
	
	public void parseNewDiff(TagComparator leftComparator, TagComparator rightComparator, RangeDifference[] differences) throws Exception {
		
		List<PublicRangeDifference> pdifferences = preProcess(differences,leftComparator);
		
		int line=0;
        
		for(int i=0;i<pdifferences.size();i++){
        	
			int start = line;//(line>0)?line-1:0; //obsolete
        	if(pdifferences.get(i).rightStart()>start)
        		markup.addClearPart(rightComparator.substring(start, pdifferences.get(i).rightStart()));
        	
        	if(pdifferences.get(i).leftLength()>0)
        		markup.addRemovedPart(leftComparator.substring(pdifferences.get(i).leftStart(),pdifferences.get(i).leftEnd()));
        	
        	if(pdifferences.get(i).leftLength()>0 && pdifferences.get(i).rightLength()>0)
        		markup.addSeperator();
        	        	
        	if(pdifferences.get(i).rightLength()>0)
        		markup.addAddedPart(rightComparator.substring(pdifferences.get(i).rightStart(), pdifferences.get(i).rightEnd()));
        	
        	line=pdifferences.get(i).rightEnd();
        }
		
		int start = line;//(line>0)?line-1:0; //obsolete
    	
		if(start<rightComparator.getRangeCount())
			markup.addClearPart(rightComparator.substring(start));
        
		
	}


	private List<PublicRangeDifference> preProcess(RangeDifference[] differences
			, TagComparator leftComparator) {
		List<PublicRangeDifference> newRanges = new LinkedList<PublicRangeDifference>();
		
		for(int i=0;i<differences.length;i++){
			
			int leftStart = differences[i].leftStart();
			int leftEnd = differences[i].leftEnd();
			int rightStart = differences[i].rightStart();
			int rightEnd = differences[i].rightEnd();
			int kind = differences[i].kind();
			
			if(i+1<differences.length){
				while(differences[i+1].kind()==kind
						&& differences[i+1].leftStart()<=leftEnd+3
						&& differences[i+1].rightStart()<=rightEnd+3
						&& differences[i+1].rightLength()>0
						&& differences[i+1].leftLength()>0
						){
					leftEnd = differences[i+1].leftEnd();
					rightEnd = differences[i+1].rightEnd();
					i++;
					}
			}
			System.out.println(leftStart+"->"+leftEnd);
			newRanges.add(new PublicRangeDifference(kind, rightStart, rightEnd-rightStart
					, leftStart, leftEnd-leftStart));
		}
		
		return newRanges;
	}

}
