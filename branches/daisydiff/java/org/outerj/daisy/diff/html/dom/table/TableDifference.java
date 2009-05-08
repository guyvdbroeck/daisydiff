package org.outerj.daisy.diff.html.dom.table;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import org.outerj.daisy.diff.html.dom.Range;
import org.outerj.daisy.diff.html.dom.TagNode;

public class TableDifference extends RangeDifference {

	public static final int TABLE_REMOVED = 6;
	public static final int TABLE_INSERTED = 7;
	public static final int TABLE_DIFF = 8;
	public static final int ROW_REMOVED = 10;
	public static final int ROW_ADDED = 11;
	public static final int ROW_SUBSTITUTION = 12;
	public static final int ROW_SPLIT = 13;
	public static final int ROW_MERGED = 14;
	public static final int COLUMN_REMOVED = 15;
	public static final int COLUMN_ADDED = 16;
	public static final int COLUMN_SUBSTITUTION = 17;

	private Range leftRange;
	private Range rightRange;
	private Range leftCommonRange;
	private Range rightCommonRange;
	private TagNode oldTableTag;
	private TagNode newTableTag;
	private LinkedList<RangeDifference> textDiff;
	private List<RangeDifference> rowDiffs;
	private List<RangeDifference> colDiffs;
	private List<RangeDifference> cellDiffs;
	private int headingDiffsIdx;
	private int tailingDiffsIdx;
	private boolean sameDimension;
	
	private TableModel leftTable;
	private TableModel rightTable;

	public TableDifference(
			int kind,
			int rightStart, int rightLength,
			int leftStart, int leftLength,
			int rightCommonStart, int rightCommonEnd,
			int leftCommonStart, int leftCommonEnd,
			Range rightRange, Range leftRange,
			TagNode newTableNode, TagNode oldTableNode,
			LinkedList<RangeDifference> plainTextDifference) {
		super(kind, 
			  rightStart, rightLength, 
			  leftStart, leftLength);
		this.leftRange = leftRange;
		this.rightRange = rightRange;
		oldTableTag = oldTableNode;
		newTableTag = newTableNode;
		textDiff = plainTextDifference;
		leftCommonRange = new Range(
				leftCommonStart, leftCommonEnd);
		rightCommonRange = new Range(
				rightCommonStart, rightCommonEnd);
	}
	
	public TagNode getOldTableTag(){
		return oldTableTag;
	}

	public TagNode getNewTableTag(){
		return newTableTag;
	}
	
	public List<RangeDifference> execute(){
		//create models
		leftTable = new TableModel(oldTableTag, leftRange);
		rightTable = new TableModel(newTableTag, rightRange);
		
		//figure if they have common content
		if (leftTable.hasCommonContentWith(rightTable)){
	    	//if yes - compare as tables
			if ((leftTable.getColumnCount() == rightTable.getColumnCount()) &&
					(leftTable.getRowCount() == rightTable.getRowCount())){
				sameDimension = true;
			} else {
				sameDimension = false;
			}
			
			//2).go by row first
			//2a). find the difference the regular way
			CellSetComparator oldRowComp = 
				new CellSetComparator(leftTable.getRows());
			CellSetComparator newRowComp = 
				new CellSetComparator(rightTable.getRows());
	        RangeDifference[] rawRowDiffs = 
	        	RangeDifferencer.findDifferences(oldRowComp, newRowComp);
	        //2b). refine it
	        rowDiffs = refineRowDiff(rawRowDiffs, sameDimension);
			//3).go by columns next
	        //3a). find the difference the regular way
	        CellSetComparator oldColumnComp = 
	        	new CellSetComparator(leftTable.getColumns());
	        CellSetComparator newColumnComp = 
	        	new CellSetComparator(rightTable.getColumns());
	        RangeDifference[] rawColDiffs = 
	        	RangeDifferencer.findDifferences(oldColumnComp, newColumnComp);
	        //3b). refine it
	        colDiffs = refineColDiff(rawColDiffs, sameDimension);
			//4).go by rows again, finding cell diffs and marking nodes
	        if (sameDimension){
	        	getCoordCellDiff();
	        } else {
	        	getCellDiff();
	        }
	    	//do the marking?
	        return new LinkedList<RangeDifference>();
		} else {
			//if no common content -
			LinkedList<RangeDifference> result = 
				new LinkedList<RangeDifference>();
			//mark one as fully deleted 
			RangeDifference diff = 
				new RangeDifference(
						TableDifference.TABLE_REMOVED,
						rightRange.getStart(), 0,
						leftRange.getStart(), leftRange.getLength());
			result.add(diff);
			//and another as fully inserted
			diff = new RangeDifference(
					TableDifference.TABLE_INSERTED,
					rightRange.getStart(), rightRange.getLength(),
					leftRange.getEnd() + 1, 0);
			result.add(diff);
			return result;
		}
		
	}
	
	protected void getCellDiff(){
		
	}
	
	protected void getCoordCellDiff(){
		//what we need to do is:
		//1. if new table starts later than the old - 
		//      then the "uncommon beginning" is listed ahead of the row diff
		//      else we need to separate deletions to place them
		//           before the table and the rest is "intermixed" with the row
		//           diffs
		//2. from the common piece remove textual diff in the rows
		//   that were "added" and "deleted" completely and "substituted"
		//3. from the common piece in all other rows 
		//   (merged, split, "common content") we need to remove
		//    textual diff in the columns that were "added" or "deleted"
		//4. from the "uncommon tail" if the old table ends earlier
		//   we need to separate deletions to place the after the table.
		
		LinkedList<RangeDifference> result = new LinkedList<RangeDifference>();
		
		LinkedList<RangeDifference> uBeginning = 
			new LinkedList<RangeDifference>();
		
		
		Iterator<?> rightRows = rightTable.getRows().iterator();
		TableRowModel rightTRow = (TableRowModel)rightRows.next();
		Range rightTRowRange = rightTRow.getRange(); 
		Iterator<RangeDifference> plainDiffs = textDiff.iterator();
		int plainDiffIdx = 0;
		RangeDifference plainDiff = plainDiffs.next();
		//1a. Does the new table start later?
		boolean newTableEarly = 
			(rightRange.getStart() < rightCommonRange.getStart());
		boolean oldTableEarly = 
			(leftRange.getStart() < leftCommonRange.getStart());
		if (newTableEarly || oldTableEarly){
			
		}
		
/*		
		while (rightCommonRange.doesNotContain(rightCurrentRow.getRange()) &&
			   rightRows.hasNext()){
			rightCurrentRow = (TableRowModel)rightRows.next();
		}
		rightCommonRowIdx = rightCurrentRow.getIndex();
		
		
		
		
		
		int plainDiffIdx = 0;
		RangeDifference currentPlainDiff = plainDiffs.next();
		while (plainDiffIdx < this.headingDiffsIdx && plainDiffs.hasNext()){
			currentPlainDiff = plainDiffs.next();
			plainDiffIdx++;
		}
		for(RangeDifference rowDiff : rowDiffs){
			int rightRowDiffStart = rowDiff.rightStart();
			//only in the common table part
			if (rightCommonRowIdx <= rightRowDiffStart){
				//gather common content rows before the diff
				//a). Is plainDiff is up-to-row?
				while(currentRightRowRange.doesNotContain(
						currentPlainDiff, Range.RIGHT) &&
					  plainDiffIdx < this.tailingDiffsIdx &&
					  plainDiffs.hasNext()){
					currentPlainDiff = plainDiffs.next();
					plainDiffIdx++;
				}
				//b).
			}
			
		}
*/
	}
	
	
	protected List<RangeDifference> refineColDiff(
			RangeDifference[] colDiffs, boolean sameDimension){
		LinkedList<RangeDifference> result = 
			new LinkedList<RangeDifference>();
		RangeDifference temp;
		//determine if the deleted or inserted columns
		//are the result of splitting/merging of the columns
		for (int i = 0; i < colDiffs.length; i++){
			RangeDifference colDiff = colDiffs[i];
			int rightStart = colDiff.rightStart();
			int rightLength = colDiff.rightLength();
			int leftStart = colDiff.leftStart();
			int leftLength = colDiff.leftLength();
			if (sameDimension){
        		int minLength = (leftLength < rightLength)? leftLength : rightLength;
        		for (int rowSubsCount = 0; rowSubsCount < minLength; rowSubsCount++){
        			temp = new RangeDifference(
        					TableDifference.COLUMN_SUBSTITUTION,
        					rightStart++, 1,
        					leftStart++, 1);
        			result.add(temp);
        			rightLength--;
        			leftLength--;
        		}
        		//now the tail
        		int kind;
        		if (rightStart < colDiff.rightEnd()){
        			//means insertions left
        			kind = TableDifference.COLUMN_ADDED;
        		} else {
        			//means deletions left
        			kind = TableDifference.COLUMN_REMOVED;
        		}
    			temp = new RangeDifference(
    					kind,
    					rightStart, rightLength,
    					leftStart, leftLength);
    			result.add(temp);
			} else {
				if (leftLength > 0){
					temp = new RangeDifference(
							TableDifference.COLUMN_REMOVED,
							rightStart, 0,
							leftStart, leftLength);
					result.add(temp);
				}
				if (rightLength > 0){
					temp = new RangeDifference(
							TableDifference.COLUMN_ADDED,
							rightStart, rightLength,
							colDiff.leftEnd(), 0);
				}
			}
		}
		return result;
	}
	
	protected List<RangeDifference> refineRowDiff(
			RangeDifference[] rowDiffs, boolean sameDimension){
		LinkedList<RangeDifference> result = 
			new LinkedList<RangeDifference>();
		RangeDifference temp;
        //determine if the deleted or inserted rows
        //are the result of splitting/merging of the rows
        for (int i = 0; i < rowDiffs.length; i++){
        	RangeDifference rowDiff = rowDiffs[i];
        	int rightStart = rowDiff.rightStart();
        	int rightLength = rowDiff.rightLength();
        	int leftStart = rowDiff.leftStart();
        	int leftLength = rowDiff.leftLength();
        	//splitting/merging can only happen if 
        	//the change is on one side
        	//otherwise we would find common content
        	if (rightLength == 0){
    			int leftEnd = rowDiff.leftEnd();
        		//then it's deletion that might be "merging"
        		//--get last common row if any on the right side
				int mergeLength = 0;
        		if (rightStart > 0){
        			ICellSet previousRow = rightTable.getRow(rightStart - 1);
        			ICellSet deletedRow = leftTable.getRow(leftStart);
        			if (previousRow != null){
        				//-- if got the last common row on the right side
        				//see if it is a result of merge
        				while (leftStart < leftEnd && 
        					   deletedRow != null &&
        					   previousRow.hasCommonContent(deletedRow)){
        					mergeLength++;
        					leftStart++;
        					leftLength--;
        					deletedRow = leftTable.getRow(leftStart);
        				}
        				if (mergeLength > 0){
        					//means we found merge
        					temp = new RangeDifference(
        							TableDifference.ROW_MERGED,
        							rightStart - 1, 1,
        							rowDiff.leftStart() - 1, mergeLength + 1);
        					result.add(temp);
        				}
        			}
        		}
				//is there anything left?
				if (leftLength > 0){
					//then check for merge with the next row
					mergeLength = 0;
					ICellSet nextRow = rightTable.getRow(rightStart);
					ICellSet deletedRow = leftTable.getRow(leftEnd - 1);
					if (nextRow != null){
						while(leftStart < leftEnd &&
							  deletedRow != null &&
							  nextRow.hasCommonContent(deletedRow)){
							mergeLength++;
							leftEnd--;
							leftLength --;
							deletedRow = leftTable.getRow(leftEnd - 1);
						}
					}
					//the merge diff follows the middle part if it exists
					if (leftLength > 0){
						//means some rows were not merged, just deleted
						temp = new RangeDifference(
								TableDifference.ROW_REMOVED,
								rightStart, 0,
								leftStart, leftLength);
						result.add(temp);
					}
					if (mergeLength > 0){
						//means we found merge with the next
						temp = new RangeDifference(
								TableDifference.ROW_MERGED,
								rightStart, 1,
								leftEnd, mergeLength + 1);
						result.add(temp);
					}
				}
        	} else if (leftLength == 0){
    			int rightEnd = rowDiff.rightEnd();
        		//then it's insertion that might be "row split"
        		//--get last common row if any on the left side
				int splitLength = 0;
        		if (leftStart > 0){
        			ICellSet previousRow = leftTable.getRow(leftStart - 1);
        			ICellSet insertedRow = rightTable.getRow(rightStart);
        			if (previousRow != null){
        				//-- if got the last common row on the left side
        				//see if it is a source of split
        				while (rightStart < rightEnd && 
        						insertedRow != null &&
        					   previousRow.hasCommonContent(insertedRow)){
        					splitLength++;
        					rightStart++;
        					rightLength--;
        					insertedRow = rightTable.getRow(rightStart);
        				}
        				if (splitLength > 0){
        					//means we found split
        					temp = new RangeDifference(
        							TableDifference.ROW_SPLIT,
        							rowDiff.rightStart() - 1, splitLength + 1,
        							leftStart - 1, 1);
        					result.add(temp);
        				}
        			}
        		}
				//is there anything left?
				if (rightLength > 0){
					//then check for split from the next row
					splitLength = 0;
					ICellSet nextRow = leftTable.getRow(leftStart);
					ICellSet insertedRow = rightTable.getRow(rightEnd - 1);
					if (nextRow != null){
						while(rightStart < rightEnd &&
							  insertedRow != null &&
							  nextRow.hasCommonContent(insertedRow)){
							splitLength++;
							rightEnd--;
							rightLength --;
							insertedRow = rightTable.getRow(rightEnd - 1);
						}
					}
					//the split diff follows the middle part if it exists
					if (rightLength > 0){
						//means some rows were not split, just inserted
						temp = new RangeDifference(
								TableDifference.ROW_ADDED,
								rightStart, rightLength,
								leftStart, 0);
						result.add(temp);
					}
					if (splitLength > 0){
						//means we found merge with the next
						temp = new RangeDifference(
								TableDifference.ROW_SPLIT,
								rightEnd, splitLength + 1,
								leftStart, 1);
						result.add(temp);
					}
				}
        	} else {//both deletion and insertion
        		int minLength = (leftLength < rightLength)? leftLength : rightLength;
        		if (sameDimension){
	        		for (int rowSubsCount = 0; rowSubsCount < minLength; rowSubsCount++){
	        			temp = new RangeDifference(
	        					TableDifference.ROW_SUBSTITUTION,
	        					rightStart++, 1,
	        					leftStart++, 1);
	        			result.add(temp);
	        			rightLength--;
	        			leftLength--;
	        		}
	        		//now the tail
	        		int kind;
	        		if (rightStart < rowDiff.rightEnd()){
	        			//means insertions left
	        			kind = TableDifference.ROW_ADDED;
	        		} else {
	        			//means deletions left
	        			kind = TableDifference.ROW_REMOVED;
	        		}
	    			temp = new RangeDifference(
	    					kind,
	    					rightStart, rightLength,
	    					leftStart, leftLength);
	    			result.add(temp);
        		} else {
        			if (leftLength > 0){
        				temp = new RangeDifference(
        						TableDifference.ROW_REMOVED,
        						rightStart, 0, 
        						leftStart, leftLength);
        				result.add(temp);
        			}
        			if (rightLength > 0){
        				temp = new RangeDifference(
        						TableDifference.ROW_ADDED,
        						rightStart, rightLength,
        						rowDiff.leftEnd(), 0);
        				result.add(temp);
        			}
        		}
        	}
        }
		return result;
	}
	
	public int getCommonLeftEnd(){
		return this.leftCommonRange.getEnd() + 1;
	}
	
	public int getCommonRightEnd(){
		return this.rightCommonRange.getEnd() + 1;
	}
}
