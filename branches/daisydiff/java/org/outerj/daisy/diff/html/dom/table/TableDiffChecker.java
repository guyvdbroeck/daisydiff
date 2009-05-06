package org.outerj.daisy.diff.html.dom.table;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.outerj.daisy.diff.html.TextNodeComparator;
import org.outerj.daisy.diff.html.dom.Range;
import org.outerj.daisy.diff.html.dom.TagNode;
import org.outerj.daisy.diff.html.dom.TextNode;

public class TableDiffChecker {
	
	private TextNodeComparator leftComp = null;
	private TextNodeComparator rightComp = null;
	
	private List<RangeDifference> originalDiffs = null;
	private List<RangeDifference> result = null;
	private List<Range> leftTables = null;
	private List<Range> rightTables = null;
	private List<RangeDifference> diffsInLeftTable = 
		new LinkedList<RangeDifference>();
	private List<RangeDifference> diffsInRightTable = 
		new LinkedList<RangeDifference>();
	private List<RangeDifference> inTableDiffs;
	
	private RangeDifference currentDiff;
	private Range leftTable;
	private Range rightTable;

	private int diffIdx = -1,  leftIdx = 0,    rightIdx = 0;
	private int diffCount = 0, leftTCount = 0, rightTCount = 0;
	private int currentlyProcessingDiffIdx = 0;
	private int firstInTableDiffIdx = -1;
	
	private boolean completeDeletion = false;
	private boolean completeInsertion = false;
	
	/**************************************************************************
     * This method needs to process difference list
     * and substitute regular differences with 
     * table differences as needed, so the table differences
     * could be processed separately later.
     * @param oldComp
     * @param newComp
     * @param differences
     * @return difference list that might contain:
     * <ol>
     *   <li>all kind of differences the original list had</li>
     *   <li><code>TABLE_DIFF</code> kind of difference</li>
     * </ol>
     * <p>
     *   In the 1st case the difference will be processed as before<br>
     *   In 2nd case the difference will be processed according to Issue 8
     *   proposition<br>
     *   The differences between tables that will be processed as before
     *   include:<br>
     *   nested tables<br>
     *   cases where one table in the old doc split into several tables 
     *   in the new doc or vice versa)<br>
     *   fully deleted or fully inserted tables (not sure yet).
     * </p>
     */
	public List<RangeDifference> tableCheck(
    		TextNodeComparator oldComp, 
    		TextNodeComparator newComp, 
    		List<RangeDifference> differences){
    	if (oldComp == null || newComp == null || differences == null){
    		throw new IllegalArgumentException(
    				"Parameters for table check cannot be nulls!");
    	}
    	
    	//no tables - no headache; old way
    	//no differences - nothing to do
    	if (!oldComp.hasTableContent() || 
    		!newComp.hasTableContent() ||
    		differences.size() == 0){
    		return differences;
    	}
    	
    	//do initialization
    	leftComp = oldComp;
    	rightComp = newComp;
    	
    	//notice these lists arrive with sorted content
    	leftTables = oldComp.getTablesRanges();
    	rightTables = newComp.getTablesRanges();

    	//diffs
    	originalDiffs = differences;
    	Iterator<RangeDifference> processingDiffs = 
    		originalDiffs.iterator();
    	
    	result = new LinkedList<RangeDifference>();
    	
    	//all kind of initialization
    	diffIdx = -1; leftIdx = 0; rightIdx = 0;
    	diffCount = originalDiffs.size();
    	leftTCount = leftTables.size();
    	rightTCount = rightTables.size();
    	leftTable = leftTables.get(leftIdx);
    	rightTable = rightTables.get(rightIdx);
    	
    	//repeat the process of finding appropriate cases
    	//for the table difference while we won't run out of 
    	//either of the lists.
    	while (diffIdx < (diffCount - 1) && 
    		   leftIdx < leftTCount && 
    		   rightIdx < rightTCount){
    		inTableDiffs = new LinkedList<RangeDifference>();
	    	if (findDeletionInTables() &&
	    		!tableHasNestedTable(leftTable, leftTables, leftIdx) &&
	    		!tableHasNestedTable(rightTable, rightTables, rightIdx)){
	    		//it might be the case we want to handle as table diff
	    		
	    		//check for overlapping tables in front
	    		if (noOverlappingTablesInFront() && 
	    			noOverlappingTablesBehind()){
	    			//append all the normal diffs
	    			//that will precede the table diff
	    			while(processingDiffs.hasNext() && 
	    					currentlyProcessingDiffIdx < firstInTableDiffIdx){
	    				result.add(processingDiffs.next());
	    				currentlyProcessingDiffIdx++;
	    			}
	    			int rightStart, rightEnd, leftStart, leftEnd;
	    			//separate "out of tables" parts of the firstInTable diff
	    			//a). in front
	    			RangeDifference edgeTDiff = inTableDiffs.get(0);
	    			int leftOuterDiffLength = 0;
	    			int leftPosition = leftTable.getRelativePosition(
	    					edgeTDiff, Range.LEFT);
	    			if (leftPosition == Range.INTERSECTS_FOLLOWS){
	    				leftOuterDiffLength = 
	    					leftTable.getStart() - edgeTDiff.leftStart();
	    			}
	    			int rightOuterDiffLength = 0;
	    			int rightPosition = rightTable.getRelativePosition(
	    					edgeTDiff, Range.RIGHT);
	    			if (rightPosition == Range.INTERSECTS_FOLLOWS){
	    				rightOuterDiffLength = 
	    					rightTable.getStart() - edgeTDiff.rightStart();
	    			}
	    			rightStart = edgeTDiff.rightStart() + rightOuterDiffLength;
	    			leftStart = edgeTDiff.leftStart() + leftOuterDiffLength;
	    			RangeDifference piece = null;
	    			if (leftOuterDiffLength > 0 ||rightOuterDiffLength > 0){
	    				//add the diff outside of the tables to the result
	    				piece = new RangeDifference(
	    					edgeTDiff.kind(),
	    					edgeTDiff.rightStart(),
	    					rightOuterDiffLength,
	    					edgeTDiff.leftStart(),
	    					leftOuterDiffLength);
	    				result.add(piece);
	    				//substitute first in-tables diff with the one that
	    				//will be completely inside one of the tables
	    				piece =  new RangeDifference(
	    					edgeTDiff.kind(),
	    					rightStart,
	    					edgeTDiff.rightLength() - rightOuterDiffLength,
	    					leftStart,
	    					edgeTDiff.leftLength() - leftOuterDiffLength);
	    				inTableDiffs.remove(0);
	    				inTableDiffs.add(0, piece);
	    			}
	    			
	    			//b). separate diff behind the tables
	    			piece = null;
	    			edgeTDiff = inTableDiffs.get(inTableDiffs.size() - 1);
	    			//see if the last in-table diff is the current.
	    			//if not - we have to move diffIdx back to not skip 
	    			//the current
	    			if (edgeTDiff != currentDiff){
	    				diffIdx--;
	    			}
	    			//now figure if the last in-table diff goes past the tables
	    			leftOuterDiffLength = 0;
	    			leftPosition = leftTable.getRelativePosition(
	    					edgeTDiff, Range.LEFT);
	    			if (leftPosition == Range.PRECEDES_INTERSECTS){
	    				leftOuterDiffLength = 
	    					edgeTDiff.leftEnd() - leftTable.getEnd() - 1;
	    			}
	    			rightOuterDiffLength = 0;
	    			rightPosition = rightTable.getRelativePosition(
	    					edgeTDiff, Range.RIGHT);
	    			if (rightPosition == Range.PRECEDES_INTERSECTS){
	    				rightOuterDiffLength = 
	    					edgeTDiff.rightEnd() - rightTable.getEnd() - 1;
	    			}
	    			rightEnd = edgeTDiff.rightEnd() - rightOuterDiffLength;
	    			leftEnd = edgeTDiff.leftEnd() - leftOuterDiffLength;
	    			if (leftOuterDiffLength > 0 || rightOuterDiffLength > 0){
	    				//substitute the last in-table diff with the one
	    				//that will be completely inside one of the tables.
	    				piece = new RangeDifference(
	    					edgeTDiff.kind(),
	    					edgeTDiff.rightStart(),
	    					edgeTDiff.rightLength() - rightOuterDiffLength,
	    					edgeTDiff.leftStart(),
	    					edgeTDiff.leftLength() - leftOuterDiffLength);
	    				inTableDiffs.remove(inTableDiffs.size() - 1);
	    				inTableDiffs.add(piece);
	    				//prepare the piece to insert after the table diff
	    				piece = new RangeDifference(
	    					edgeTDiff.kind(),
	    					rightEnd,
	    					rightOuterDiffLength,
	    					leftEnd,
	    					leftOuterDiffLength);
	    			}
	    			//create table difference 
	    			TagNode leftTableNode = TableModel.getTableAncestor(
	    					leftComp.getTextNode(leftTable.getStart()));
	    			TagNode rightTableNode = TableModel.getTableAncestor(
	    					rightComp.getTextNode(rightTable.getStart()));
	    			TableDifference tableDiff = 
	    				new TableDifference(
	    						TableDifference.TABLE_DIFF,
	    						rightStart, rightEnd - rightStart,
	    						leftStart, leftEnd - leftStart,
	    						rightTable, leftTable,
	    						rightTableNode, leftTableNode,
	    						inTableDiffs
	    						);
	    			//add the table diff to the result
	    			List<RangeDifference> temp = tableDiff.execute();
	    			if (temp != null){
	    				result.addAll(temp);
	    			}
	    			//add the "outer" piece if it exists
	    			if (piece != null){
	    				result.add(piece);
	    			}
	    			//move the "currentlyProcessingDiffIdx" 
	    			//to the current diff to skip all the normal diff
	    			//that we have replaced with table diff
	    			currentlyProcessingDiffIdx = 
	    				firstInTableDiffIdx + inTableDiffs.size();
	    			//resume processing
	    			
	    		} 
	    	}
    	}
    	//release not needed resources
    	return result;
	}

	protected boolean noOverlappingTablesBehind(){
		//only if there are more differences
		boolean pastLeftTable = false;
		boolean pastRightTable = false;
		if (diffIdx < diffCount - 1){
			do{
				//go to the next diff
				diffIdx++;
				currentDiff = originalDiffs.get(diffIdx);
				if (!pastLeftTable){
					pastLeftTable = 
						leftTable.doesNotContain(currentDiff, Range.LEFT);
				}
				if (!pastLeftTable){
					this.diffsInLeftTable.add(currentDiff);
					inTableDiffs.add(currentDiff);
				}
				if (!pastRightTable){
					pastRightTable = 
						rightTable.doesNotContain(currentDiff, Range.RIGHT);
				}
				if (!pastRightTable){
					this.diffsInRightTable.add(currentDiff);
					if (pastLeftTable){
						inTableDiffs.add(currentDiff);
					}
				}
			} while ((!pastLeftTable || !pastRightTable) &&
					diffIdx < diffCount - 1);
			//now we are either out of diffs or currentDiff is
			//past both tables.
		}
		//a). take the last table diff:
		RangeDifference lastTDiff = 
			inTableDiffs.get(inTableDiffs.size() - 1);
		//b). how the last difference fits into the tables?
		int leftPosition = leftTable.getRelativePosition(
				lastTDiff, Range.LEFT);
		int rightPosition = rightTable.getRelativePosition(
				lastTDiff, Range.RIGHT);
		if (leftPosition == Range.SUBSETS){
			completeDeletion = true;
		}
		if (rightPosition == Range.SUBSETS){
			completeInsertion = true;
		}
		if (completeInsertion || completeDeletion){
			return false;//don't need table diff - insertion or deletion
		}
		//c). Which table ends later?
		Range overlapCandidate = null;
		int idxOfLastSurvivor = Range.NOT_DEFINED;
		int idxOfLastLeftTableSurvivor = Range.NOT_DEFINED;
		int idxOfLastRightTableSurvivor = Range.NOT_DEFINED;
		int leftSurvivorsTailLength = Range.NOT_DEFINED;
		int rightSurvivorsTailLength = Range.NOT_DEFINED;
		boolean checkSide = false;
		switch (leftPosition){
			case Range.PRECEDES:
				//means the right table is longer and only it contains 
				//the last difference. 
				if (!(leftIdx < (leftTCount - 1))){
					//no more tables on the left side = no overlap behind
					return true;
				}
				break;
			case Range.CONTAINS:
			case Range.INCLUDES_FOLLOWS:
			case Range.INTERSECTS_FOLLOWS:
				//means the left table continues after the last diff
				idxOfLastLeftTableSurvivor = leftTable.getEnd();
				//the following length value is 1 shorter than 
				//it is in the reality, because the diff end points to 
				//the next after diff element
				leftSurvivorsTailLength = 
					leftTable.getEnd() - lastTDiff.leftEnd();
				break;
			case Range.MATCHES:
			case Range.PRECEDES_INCLUDES:
			case Range.PRECEDES_INTERSECTS:
				//means the left table contains only part of the last diff
				//so the last survivor is before the last diff
				idxOfLastLeftTableSurvivor = lastTDiff.leftStart() - 1;
				break;
			default: //shouldn't get here - some kind of error
				return false;
		}
		switch(rightPosition){
			case Range.PRECEDES:
				//means right table doesn't include the last diff
				if (!(rightIdx < (rightTCount - 1))){
					//no more tables on the right side = no overlap behind
					return true;
				}
				break;
			case Range.CONTAINS:
			case Range.INCLUDES_FOLLOWS:
			case Range.INTERSECTS_FOLLOWS:
			    //means the right table is continued
				idxOfLastRightTableSurvivor = rightTable.getEnd(); 
				//the following length value is 1 shorter than 
				//it is in the reality, because the diff end points to 
				//the next after diff element
				rightSurvivorsTailLength = 
					rightTable.getEnd() - lastTDiff.rightEnd();
				break;
			case Range.MATCHES:
			case Range.PRECEDES_INCLUDES:
			case Range.PRECEDES_INTERSECTS:
				//means last survivor is before last diff
				idxOfLastRightTableSurvivor = 
					lastTDiff.rightStart() - 1;
				break;
			default:
				//shouldn't get here, 
				//as it is FOLLOWS case
				//means some kind of error
				return false; 
		}//end of switch on the right side

		if ((idxOfLastLeftTableSurvivor != Range.NOT_DEFINED) &&
			(idxOfLastRightTableSurvivor != Range.NOT_DEFINED) &&
			(rightSurvivorsTailLength == leftSurvivorsTailLength)){
			//means the tables end with the same TextNode = no overlap
			return true;
		} else if (rightSurvivorsTailLength < leftSurvivorsTailLength){
			//means right table ends first and 
			//the left table lasts past the last diff
			checkSide = Range.RIGHT;
			idxOfLastSurvivor = 
				lastTDiff.rightEnd() + leftSurvivorsTailLength;
		} else if (leftSurvivorsTailLength < rightSurvivorsTailLength){
			//means left table ends first and
			//the right table lasts past the last diff 
			checkSide = Range.LEFT;
			idxOfLastSurvivor = 
				lastTDiff.leftEnd() + rightSurvivorsTailLength;
		} else {
			//means one table doesn't include the diff
			//and both tables end before the last diff does
			if (idxOfLastLeftTableSurvivor == Range.NOT_DEFINED){
				checkSide = Range.LEFT;
				idxOfLastSurvivor = lastTDiff.leftStart() - 1;
			} else {
				checkSide = Range.RIGHT;
				idxOfLastSurvivor = lastTDiff.rightStart() - 1;
			}
		}
		//d).now we only need the next table on the check side if it exists.
		if (checkSide == Range.LEFT){
			if (leftIdx < (leftTCount - 1)){
				overlapCandidate = leftTables.get(leftIdx + 1);
			} else {
				return true;//no more tables - no overlap behind
			}
		} else {
			if (rightIdx < (rightTCount - 1)){
				overlapCandidate = rightTables.get(rightIdx + 1);
			} else {
				return true;
			}
		}
		//e). The overlap check
		int nextPosition = 
			overlapCandidate.getRelativePosition(idxOfLastSurvivor);
		return nextPosition == Range.FOLLOWS;
	}
	
	protected boolean noOverlappingTablesInFront(){
		this.inTableDiffs.clear();
		int leftTableDiffsCount = this.diffsInLeftTable.size();
		int rightTableDiffsCount = this.diffsInRightTable.size();
		boolean leftTableEarlier = false;
		boolean rightTableEarlier = false;
		//the first difference in either of the tables
		RangeDifference firstTableDiff = null;
		//the number of "retained" text nodes from the beginning
		//of the "earlier" table to the first table difference
		int commonElementsBeforeFirstDiff = -1;
		
		//we have at least one (last deletion in both tables) diff in those
		if (leftTableDiffsCount < rightTableDiffsCount){
			//means right table has started earlier
			rightTableEarlier = true;
			firstTableDiff = this.diffsInRightTable.get(0);
			commonElementsBeforeFirstDiff = 
				firstTableDiff.rightStart() - rightTable.getStart();
		} else if (rightTableDiffsCount < leftTableDiffsCount){
			//means left table has started earlier
			leftTableEarlier = true;
			firstTableDiff = this.diffsInLeftTable.get(0);
			commonElementsBeforeFirstDiff = 
				firstTableDiff.leftStart() - leftTable.getStart();
		} else {
			//means all differences in the tables are shared
			//diff list is the same on both sides
			firstTableDiff = this.diffsInLeftTable.get(0);
			//which table has started earlier?
			int leftBeginningCount = 
				firstTableDiff.leftStart() - leftTable.getStart();
			TextNode check = leftComp.getTextNode(leftTable.getStart());
			check = leftComp.getTextNode(firstTableDiff.leftStart());
			int rightBeginningCount = 
				firstTableDiff.rightStart() - rightTable.getStart();
			check = rightComp.getTextNode(rightTable.getStart());
			check = rightComp.getTextNode(firstTableDiff.rightStart());
			if (leftBeginningCount < rightBeginningCount){
				//right table has started earlier
				rightTableEarlier = true;
				commonElementsBeforeFirstDiff = rightBeginningCount;
			} else if (rightBeginningCount < leftBeginningCount){
				//left table has started earlier
				leftTableEarlier = true;
				commonElementsBeforeFirstDiff = leftBeginningCount;
			}
		}
		if (commonElementsBeforeFirstDiff < 0){
			//this means the difference started before table and 
			//continued into the table. Then there were no common elements
			//before the difference.
			commonElementsBeforeFirstDiff = 0;
		}
		if (rightTableEarlier){
			this.inTableDiffs.addAll(this.diffsInRightTable);
			firstInTableDiffIdx = diffIdx + 1 - rightTableDiffsCount;
			if (leftIdx == 0){
				return true;
			}
			TextNode firstCommon = leftComp.getTextNode(
					firstTableDiff.leftStart() - 
					commonElementsBeforeFirstDiff);
			return noOverlapInFrontCheck(
					commonElementsBeforeFirstDiff,
					firstTableDiff.leftStart(),
					leftIdx,
					leftTables);
		} else if (leftTableEarlier){
			this.inTableDiffs.addAll(this.diffsInLeftTable);
			firstInTableDiffIdx = diffIdx + 1 - leftTableDiffsCount;
			if (rightIdx == 0){
				return true;
			}
			TextNode firstCommon = rightComp.getTextNode(
					firstTableDiff.rightStart() - 
					commonElementsBeforeFirstDiff);
			return noOverlapInFrontCheck(
					commonElementsBeforeFirstDiff,
					firstTableDiff.rightStart(),
					rightIdx,
					rightTables);
		} else {
			//means the table started from the same textNode
			this.inTableDiffs.addAll(this.diffsInLeftTable);
			firstInTableDiffIdx = diffIdx + 1 - leftTableDiffsCount;
			return true;
		}
	}
	
	protected boolean noOverlapInFrontCheck(
			int earlierCommonBeginningCount,
			int lateSideDiffStart,
			int lateSideTableIdx, 
			List<Range> lateSideTables){
		int lateSideFirstElem = 
			lateSideDiffStart - earlierCommonBeginningCount;
		//are there any other tables on the late side starting with 
		//lateSideFirstElem and ending with lateSideTable.getStart();
		int prevIdx = lateSideTableIdx - 1;
		Range lateSideTable = lateSideTables.get(lateSideTableIdx);
		Range prevTableOnLateSide = lateSideTables.get(prevIdx);
		boolean maybeNested = true;
		while (maybeNested && prevIdx > 1){
			prevIdx--;
			Range maybeSurrounding = lateSideTables.get(prevIdx);
			if (maybeSurrounding.contains(prevTableOnLateSide)){
				prevTableOnLateSide = maybeSurrounding;
			} else {
				maybeNested = false;
			}
		}
		int position = prevTableOnLateSide.getRelativePosition(
				lateSideFirstElem, lateSideTable.getStart());
		return position == Range.PRECEDES;
	}
	
	protected boolean tableHasNestedTable(
			Range table, List<Range> listOfTables, int idx){
		if (idx < listOfTables.size() - 1){//otherwise no nested tables
			Range nextTable = listOfTables.get(idx+1);
			return table.contains(nextTable);
		} else {
			return false;
		}
	}
	
	protected boolean findDeletionInTables(){
		this.diffsInLeftTable.clear();
		this.diffsInRightTable.clear();
		boolean foundDeletionInTables = false;
    	do {
    		diffIdx++;
    		currentDiff = originalDiffs.get(diffIdx);
			//are left tables up-to-diff?
			int leftPosition = 
				leftTable.getRelativePosition(currentDiff, Range.LEFT);
			while (leftPosition == Range.PRECEDES && 
				   leftIdx < leftTCount - 1){
				//left tables are behind - catch up
				this.diffsInLeftTable.clear();
				leftIdx++;
				leftTable = leftTables.get(leftIdx);
				leftPosition = leftTable.getRelativePosition(
						currentDiff, Range.LEFT);
			}
			boolean inLeftTable = !(leftPosition == Range.PRECEDES) &&
    			                  !(leftPosition == Range.FOLLOWS) &&
    			                  !(leftPosition == Range.SUBSETS);
			if (inLeftTable){
    			//we have the intersection -> add to the list
				this.diffsInLeftTable.add(currentDiff);
   			}

			//are right tables up-to-diff?
			int rightPosition = 
				rightTable.getRelativePosition(currentDiff, Range.RIGHT);
			while (rightPosition == Range.PRECEDES &&
				   rightIdx < rightTCount - 1){
				//right tables are behind - catch up
				this.diffsInRightTable.clear();
				rightIdx++;
				rightTable = rightTables.get(rightIdx);
				rightPosition = rightTable.getRelativePosition(
						currentDiff, Range.RIGHT);
			}
			boolean inRightTable = !(rightPosition == Range.PRECEDES) &&
				                   !(rightPosition == Range.FOLLOWS) &&
				                   !(rightPosition == Range.SUBSETS);
			if (inRightTable){
				//add to the rightTable list
				this.diffsInRightTable.add(currentDiff);
			}

			//we are interested in diff if there is a deletion
    		if (currentDiff.leftLength() > 0 &&
    			inLeftTable && inRightTable){
    				foundDeletionInTables = true;
    		}
    	} while (!foundDeletionInTables && 
    			 diffIdx < diffCount &&
    			 leftIdx < leftTCount &&
    			 rightIdx < rightTCount);
		return foundDeletionInTables;
	}
}
