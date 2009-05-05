package org.outerj.daisy.diff.html.dom.table;

import java.util.List;

import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.outerj.daisy.diff.html.dom.TagNode;

public class TableDifference extends RangeDifference {

	public static final int TABLE_REMOVED = 6;
	public static final int TABLE_INSERTED = 7;
	public static final int TABLE_DIFF = 8;
	public static final int COMPL_TABLE = 9;
	public static final int OUTSIDE = 10;
	public static final int D_BEFORE_T = 11;
	public static final int D_IN_T = 12;
	public static final int I_IN_T = 13;
	public static final int CH_IN_T = 14;
	
	private TagNode oldTableTag;
	private TagNode newTableTag;
	private List<RangeDifference> textDiff;

	public TableDifference(
			int kind,
			int rightStart, int rightLength,
			int leftStart, int leftLength,
			TagNode newTableNode, TagNode oldTableNode,
			List<RangeDifference> plainTextDifference) {
		super(kind, 
			  rightStart, rightLength, 
			  leftStart, leftLength);
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
}
