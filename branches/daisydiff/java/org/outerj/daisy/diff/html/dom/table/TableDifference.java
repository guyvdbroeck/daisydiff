package org.outerj.daisy.diff.html.dom.table;

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
/*
	public static final int COMPL_TABLE = 9;
	public static final int OUTSIDE = 10;
	public static final int D_BEFORE_T = 11;
	public static final int D_IN_T = 12;
	public static final int I_IN_T = 13;
	public static final int CH_IN_T = 14;
*/	
	private Range leftRange;
	private Range rightRange;
	private TagNode oldTableTag;
	private TagNode newTableTag;
	private List<RangeDifference> textDiff;
	
	private TableModel leftTable;
	private TableModel rightTable;

	public TableDifference(
			int kind,
			int rightStart, int rightLength,
			int leftStart, int leftLength,
			Range rightRange, Range leftRange,
			TagNode newTableNode, TagNode oldTableNode,
			List<RangeDifference> plainTextDifference) {
		super(kind, 
			  rightStart, rightLength, 
			  leftStart, leftLength);
		this.leftRange = leftRange;
		this.rightRange = rightRange;
		oldTableTag = oldTableNode;
		newTableTag = newTableNode;
		textDiff = plainTextDifference;
	}
	
	public TagNode getOldTableTag(){
		return oldTableTag;
	}

	public TagNode getNewTableTag(){
		return newTableTag;
	}
	
	public List<RangeDifference> execute(){
		//create models
		leftTable = new TableModel(oldTableTag);
		rightTable = new TableModel(newTableTag);
		
		//figure if they have common content
		if (leftTable.hasCommonContentWith(rightTable)){
	    	//if yes - compare as tables
			if ((leftTable.getColumnCount() == rightTable.getColumnCount()) &&
					(leftTable.getRowCount() == rightTable.getRowCount())){
				return findSameDimensionDiff();
			} else {
				return findStructuredDiff();
			}
	    	//do the marking
			
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
	
	public List<RangeDifference> findSameDimensionDiff(){
		LinkedList<RangeDifference> result = 
			new LinkedList<RangeDifference>();
		//1).go by row first
		//1a). find the difference the regular way
		CellSetComparator oldRowComp = 
			new CellSetComparator(leftTable.getRows());
		CellSetComparator newRowComp = 
			new CellSetComparator(rightTable.getRows());
        RangeDifference[] rowDiffs = 
        	RangeDifferencer.findDifferences(oldRowComp, newRowComp);
        //1b). refine it
        result.addAll(refineRowDiff(rowDiffs));
		//2).go by columns next
        //2a). find the difference the regular way
        CellSetComparator oldColumnComp = 
        	new CellSetComparator(leftTable.getColumns());
        CellSetComparator newColumnComp = 
        	new CellSetComparator(rightTable.getColumns());
        RangeDifference[] columnDiffs = 
        	RangeDifferencer.findDifferences(oldColumnComp, newColumnComp);
        //2b). refine it
        result.addAll(refineColDiff(columnDiffs));
		//3).go by cells
		return null;
	}
	
	public List<RangeDifference> findStructuredDiff(){
		LinkedList<RangeDifference> result = 
			new LinkedList<RangeDifference>();
		//add this diff for dimension diff purpose
		result.add(this);
		return result;
	}
	
	protected List<RangeDifference> refineRowDiff(RangeDifference[] rowDiffs){
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
        			CellSet previousRow = rightTable.getRow(rightStart - 1);
        			CellSet deletedRow = leftTable.getRow(leftStart);
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
        							rowDiff.leftStart(), mergeLength);
        					result.add(temp);
        				}
        			}
        		}
				//is there anything left?
				if (leftLength > 0){
					//then check for merge with the next row
					mergeLength = 0;
					CellSet nextRow = rightTable.getRow(rightStart);
					CellSet deletedRow = leftTable.getRow(leftEnd - 1);
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
								leftEnd, mergeLength);
						result.add(temp);
					}
				}
        	} else if (leftLength == 0){
    			int rightEnd = rowDiff.leftEnd();
        		//then it's insertion that might be "row split"
        		//--get last common row if any on the left side
				int splitLength = 0;
        		if (leftStart > 0){
        			CellSet previousRow = leftTable.getRow(leftStart - 1);
        			CellSet insertedRow = rightTable.getRow(rightStart);
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
        							rowDiff.rightStart(), splitLength,
        							leftStart - 1, 1);
        					result.add(temp);
        				}
        			}
        		}
				//is there anything left?
				if (rightLength > 0){
					//then check for split from the next row
					splitLength = 0;
					CellSet nextRow = leftTable.getRow(leftStart);
					CellSet insertedRow = rightTable.getRow(rightEnd - 1);
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
								rightEnd, splitLength,
								leftStart, 1);
						result.add(temp);
					}
				}
        	} else {//both deletion and insertion
        		int minLength = (leftLength < rightLength)? leftLength : rightLength; 
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
        	}
        }
		return result;
	}
}
