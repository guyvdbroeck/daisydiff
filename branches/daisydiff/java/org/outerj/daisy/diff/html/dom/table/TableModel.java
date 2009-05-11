package org.outerj.daisy.diff.html.dom.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.outerj.daisy.diff.html.dom.Node;
import org.outerj.daisy.diff.html.dom.Range;
import org.outerj.daisy.diff.html.dom.TagNode;
import org.outerj.daisy.diff.html.dom.TextNode;
import org.outerj.daisy.diff.html.modification.ModificationType;

import static org.outerj.daisy.diff.html.dom.table.TableStatics.*;

public class TableModel{
	
	//*-----------------------------------------------------------------------*
	//*                            static methods                             *
	//*-----------------------------------------------------------------------*
	
	/**************************************************************************
	 * This method can check whether the provided node is inside a table 
	 * in the TagNode tree. 
	 * @param toCheck - a node to check
	 * @return <code>null</code> if the node doesn't have a "table" TagNode 
	 * among its ancestors.<br>
	 * the "table" ancestor if it was found.
	 */
	public static TagNode getTableAncestor(Node toCheck){
		//if there's nothing to check - it's not in a table
		if (toCheck == null){
			return null;
		}
		
		//go through all parents or until table tag is found
		do {
			TagNode parent = toCheck.getParent();
			if (parent == null){
				return null;//haven't seen table among ancestors
			}
			if (TABLE_TAG_NAME.equals(parent.getQName())){
				return parent; //it's in the table!
			}
			toCheck = parent;
		} while (true);
	}
	
	
	/**************************************************************************
	 * This method counts amount of <code>TextNode</code>s inside a 
	 * <code>TagNode</code> in the TagNode tree. 
	 * @param toCheck - a node to check
	 * @return amount of descendants <code>TextNode</code>s.
	 */
	public static int getTextNodeCount(Node ancestor){
		if (ancestor == null){
			return 0;
		}
		if (ancestor instanceof TextNode){
			return 1;
		}
		TagNode parent;
		if (ancestor instanceof TagNode){
			parent = (TagNode)ancestor;
		} else {//not a TextNode, not a TagNode - what's that?!
			return 0;
		}
		int textNodeCount = 0;
		for (Node child : parent){
			textNodeCount += getTextNodeCount(child);
		}
		return textNodeCount;
	}
	
	//*-----------------------------------------------------------------------*
	//*                        Table Model itself                             *
	//*-----------------------------------------------------------------------*
	
	private TagNode tableTagNode = null;
	private Range tableRange = null;
	private ArrayList<ICellSet> rows = null;
	private ArrayList<ICellSet> columns = null;
	private TreeSet<String> content = null;
	
	protected TableModel(){
		
	}
	
	/**************************************************************************
	 * Creates a model from the table out of the <code>TagNode</code> tree.
	 * @param tableTag - table tag in the <code>TagNode</code> tree.
	 * @throws @see java.lang.IllegalArgumentException - if the 
	 * parameter is <code>null</code> or is not a table tag
	 */
	public TableModel(TagNode tableTag, Range tableRange){
		this(tableTag, tableRange, true);
	}
	
	public TableModel(TagNode tableTag){
		this(tableTag, new Range(0), false);
		this.getRange().setEnd(
				((TableRowModel)this.getLastRow()).getEndOfRange());
	}
	
	public TableModel(TagNode tableTag, Range tableRange, boolean needContent){
		this.nullCheck(tableTag);
		this.nullCheck(tableRange);
		if (!TABLE_TAG_NAME.equals(tableTag.getQName())) {
			throw new IllegalArgumentException(
					"Can only build a table from a TABLE TagNode");
		}
		//remember where we came from
		tableTagNode = tableTag;
		this.tableRange = tableRange;
		//processing the content of the table tag.
		if (tableTagNode.getNbChildren() > 0){
			int rangeIdx = Range.NOT_DEFINED;
			if (tableRange != null){
				rangeIdx = tableRange.getStart();
			}
			rows = new ArrayList<ICellSet>();
			if (needContent){
				content = new TreeSet<String>();
			}
			int idx = 0;
			//figure out rows
			appendChildRows(tableTagNode, idx, null, rangeIdx, needContent);
			populateColumns(needContent);
		}
	}

	public boolean hasCommonContentWith(TableModel another){
		this.nullCheck(another);
		if (!another.hasContent() || !this.hasContent()){
			return false;
		}
		//remember - both contents are sorted and do not contain duplicates
		Iterator<String> anotherContent = another.getContent().iterator();
		Iterator<String> myContent = getContent().iterator();
		//there's at least 1 content item or we would returned false
		String otherContentItem = anotherContent.next();
		String myContentItem = myContent.next();
		boolean moreContent = true;
		do{
			int comparisonResult = myContentItem.compareTo(otherContentItem);
			if (comparisonResult < 0){
				//means myContentItem precedes the other
				if (myContent.hasNext()){
					myContentItem = myContent.next();
				} else {
					moreContent = false;
				}
			} else if (comparisonResult > 0){
				//means myContentItem follows the other
				if (anotherContent.hasNext()){
					otherContentItem = anotherContent.next();
				} else {
					moreContent = false;
				}
			} else {//found common content
				return true;
			}
		} while (moreContent);
		return false;
	}
	
	public void markColumn(int colIdx, ModificationType kind){
		this.checkColIdxBounds(colIdx);
		((TableColumnModel)getColumn(colIdx)).mark(kind);
	}
	
	public void markColumns(
			int startWith, int amount, ModificationType kind){
		this.checkColIdxBounds(startWith);
		int notIncludedEnd = startWith + amount;
		this.checkColIdxBounds(notIncludedEnd - 1);
		for (int i = startWith; i < notIncludedEnd; i++){
			((TableColumnModel)getColumn(i)).mark(kind);
		}
	}
	
	public void markRow(int rowIdx, ModificationType kind){
		this.checkRowIdxBounds(rowIdx);
		((TableRowModel)getRow(rowIdx)).mark(kind);
	}
	
	public void markRows(
			int startWith, int amount, ModificationType kind){
		this.checkRowIdxBounds(startWith);
		int notIncludedEnd = startWith + amount;
		this.checkRowIdxBounds(notIncludedEnd - 1);
		for (int i = startWith; i < notIncludedEnd; i++){
			((TableRowModel)getRow(i)).mark(kind);
		}
	}
	
	public TableModel lightCopy(){
		TagNode copyTag = (TagNode)getTableTagNode().copyTree();
		return new TableModel(copyTag);
	}
	
	public TableModel copyModelNoStructureNoContent(){
		TableModel tableCopy = new TableModel();
		Range myRange = getRange();
		if (myRange != null){
			tableCopy.setRange(myRange.copy());
		}
		TagNode myTag = getTableTagNode();
		if (myTag != null){
			tableCopy.setTableTagNode(myTag);
		}
		int rowCount = getRowCount();
		if (0 < rowCount){
			Iterator<ICellSet> myRows = getRows().iterator();
			TableRowModel rowCopy = null; 
			while (myRows.hasNext()){
				TableRowModel myRow = (TableRowModel)myRows.next();
				rowCopy = myRow.lightCopy(rowCopy, rowCount - myRow.getIndex() - 1);
				tableCopy.appendRow(rowCopy);
			}
		}
		tableCopy.populateColumns(false);
		return tableCopy;
	}
	
	/**
	 * No content kept - "lightCopy"
	 * The span of the cells is adjusted to not span beyond first and last row
	 * of the copy. 
	 * @param startIdx
	 * @param amount
	 * @return
	 */
	public TableModel copyRangeOfRows(int startIdx, int amount){
		int notIncludedEnd = startIdx + amount;
		this.checkRowIdxBounds(startIdx);
		this.checkRowIdxBounds(notIncludedEnd - 1);
		TableModel rowExtract = new TableModel();
		TableRowModel rowCopy = null;
		for (int i = startIdx; i < notIncludedEnd; i++){
			rowCopy = ((TableRowModel)getRow(i)).lightCopy(
					rowCopy, notIncludedEnd - i - 1);
			rowExtract.appendRow(rowCopy);
		}
		Range extractRange = new Range(
				((TableRowModel)rowExtract.getRow(0)).getRange().getStart(),
				((TableRowModel)rowExtract.getLastRow()).getRange().getEnd());
		rowExtract.setRange(extractRange);
		rowExtract.updateRowIndices(0);
		rowExtract.populateColumns(false);
		return rowExtract;
	}
	
	public TableRowModel getLightCopyOfRow(int idx){
		this.checkRowIdxBounds(idx);
		return this.getRowCopy(idx).lightCopy(0);
	}
	
	/**************************************************************************
	 * During insertion of the empty columns the cells that 
	 * were spanned over the border where we insert will be 
	 * spanned over the empty columns at the end. This means,
	 * that if the cell in the 3rd row and 2nd column was
	 * spanned over 2 columns (belonged to column 2 and 3), and
	 * we requested to insert 5 empty columns with startIdx=3,
	 * the in the 3rd row the cell in the second column will now
	 * span 7 cells and belong to the columns 2 - 8.
	 * This method is used to match the structure of the deleted rows
	 * that we are inserting in the new table with the table structure.
	 * 
	 * @param startIdx the index of first empty column after it was inserted
	 * @param amount - amount of empty columns to insert.
	 */
	public void insertEmptyColumns(
			int startIdx, int amount, boolean needContent){
		//can't use standard check, because can insert after last col
		if (startIdx < 0 || getColumnCount() < startIdx){
			throw new IndexOutOfBoundsException(
					"Currently index could be from 0 to " +
					getColumnCount() + ", but " + startIdx + 
					" was provided.");
		}
		//Create column models for the inserted columns
		List<TableColumnModel> newCols = new ArrayList<TableColumnModel>();
		for (int i = 0; i < amount; i++){
			TableColumnModel newCol = new TableColumnModel(
					startIdx + i, needContent);
			newCols.add(newCol);
		}
		//insert the cells, filling the new cols as we go
		int spanned = 0;//how many rows have to update their cell's colIdx
		for (ICellSet row : getRows()){
			TableRowModel rowModel = (TableRowModel)row;
			if (spanned == 0){
				if(!rowModel.insertedEmptyCells(startIdx, amount)){
					spanned = rowModel.changeCellColSpan(
							startIdx - 1, amount).getRowSpan() - 1;
				}
			} else {
				rowModel.updateWithSpannedCell(startIdx, amount);
				spanned--;
			}
			for (int i = 0; i < amount; i++){
				newCols.get(i).appendCell(
						rowModel.getCell(startIdx + i), needContent);
			}
		}
		
		//updateColumns
		//a).insert the new column models
		this.getColumns().addAll(startIdx, newCols);
		
		//b). Update indices for the tail
		this.updateColumnIndices(startIdx + amount);
	}
	
	/**************************************************************************
	 * During insertion the cells that span over the insertion point
	 * will be split in 2. The top part will retain the content 
	 * (with the reduced span) while the bottom part will 
	 * get an empty cell spanned over the bottom part. Example:
	 * the table has a cell with a content in the row 3, column 2 that has 
	 * rowspan == 5. We are inserting new row with the desiredIdx == 4.
	 * Now the cell in the row 3, column 2 will have same content, 
	 * but its rowspan == 2. While the row 5 will have an empty cell in column 2
	 * with rowspan == 3.
	 * @param newRow - a row to insert
	 * @param desiredIdx - the index where the new row should be.
	 * @throws IndexOutOfBoundsException if the desiredIdx is 
	 * out of row collection bounds
	 */
	public void insertRow(TableRowModel newRow, int desiredIdx){
		//splitToInsertRow does the index check to throw exception if needed
		//check for the cells that are spanned over the insertion point
		splitToInsertRow(desiredIdx);
		//insert
		getRows().add(desiredIdx, newRow);
		if (this.hasContent() && newRow.hasContent()){
			this.getContent().addAll(newRow.getContent());
		}
		//update indices and columns
		updateRowIndices(desiredIdx);
		this.updateColumns(
				0, getColumnCount(), desiredIdx, getRowCount() - desiredIdx);
	}
	
	public void insertRowRange(TableModel rowRange, int startIdx){
		//splitToInsertRow does the index check to throw exception if needed
		//check for the cells that are spanned over the insertion point
		splitToInsertRow(startIdx);
		//insert
		ArrayList<ICellSet> rowsToInsert = rowRange.getRows();
		getRows().addAll(startIdx, rowsToInsert);
		if (this.hasContent() && rowRange.hasContent()){
			TreeSet<String> myContent = getContent();
			for (ICellSet row : rowsToInsert){
				if (row.hasContent()){
					myContent.addAll(row.getContent());
				}
			}
		}
		//update indices and columns
		updateRowIndices(startIdx);
		this.updateColumns(
				0, getColumnCount(), startIdx, getRowCount() - startIdx);
	}
	
	//*-----------------------------------------------------------------------*
	//*                           getters/setters                             *
	//*-----------------------------------------------------------------------*

	/**************************************************************************
	 * 
	 */
	public TreeSet<String> getContent(){
		return content;
	}
	
	public boolean hasContent(){
		if (content != null && content.size() > 0){
			return true;
		} else {
			return false;
		}
	}
	
	public int getColumnCount(){
		if (this.columns == null){
			return 0;
		} else {
			return columns.size();
		}
	}
	
	public int getRowCount(){
		if (this.rows == null){
			return 0;
		} else {
			return rows.size();
		}
	}
	
	public ArrayList<ICellSet> getRows(){
		return rows;
	}
	
	public ICellSet getRow(int idx){
		this.checkRowIdxBounds(idx);
		return rows.get(idx);
	}
	
	public ICellSet getLastRow(){
		int size = this.getRowCount();
		if (size == 0){
			return null;
		} else {
			return getRows().get(size - 1);
		}
	}
	
	public TableRowModel getRowCopy(int idx){
		this.checkRowIdxBounds(idx);
		return ((TableRowModel)rows.get(idx)).copy();
	}
	
	public List<ICellSet> getColumns(){
		return columns;
	}
	
	public ICellSet getColumn(int idx){
		this.checkColIdxBounds(idx);
		return columns.get(idx);
	}
	
	public Range getRange(){
		return tableRange;
	}
	
	public TagNode getTableTagNode(){
		return tableTagNode;
	}
	
	public void setRange(Range newValue){
		this.tableRange = newValue;
	}
	
	protected void setRows(ArrayList<ICellSet> rowCollection){
		rows = rowCollection;
	}
	
	public void setTableTagNode(TagNode tableTag){
		this.nullCheck(tableTag);
		if (!TABLE_TAG_NAME.equals(tableTag.getQName().toLowerCase())){
			throw new IllegalArgumentException(
					"Only table tag is allowed as the parameter");
		}
		tableTagNode = tableTag;
		//if there are kids - bind them?
		//no, because the rows can go through table sections (tbody etc).
	}
	
	//*-----------------------------------------------------------------------*
	//*                            helper methods                             *
	//*-----------------------------------------------------------------------*

	/**************************************************************************
	 * This method assumes that passed parent tag is the table tag or its 
	 * immediate child (like "tbody")
	 */
	protected TableRowModel appendChildRows(
			TagNode parent, int startIndex, 
			TableRowModel previousRow, int rangeStart,
			boolean needContent){
		//make sure we actually have the children tags to process
		if (parent == null || parent.getNbChildren() == 0){
			return previousRow;
		}
		int idx = startIndex;
		//we are interested in the row tags and 
		//will let the rows to handle cell tags.
		//figure out rows:
		for (Node child : parent){
			//is it a row or something else like "thead"?
			if (child != null && child instanceof TagNode){
				TagNode tagChild = (TagNode)child;
				String childName = tagChild.getQName();
				if (ROW_TAG_NAME.equals(childName)){//row child
					//if we had row-spanned cells in the previous row
					//they might belong to this one too 
					if (previousRow != null && 
						previousRow.getMaxSpanDown() >= NO_SPAN){
						previousRow = new TableRowModel(
							tagChild, idx, 
							previousRow.getSpannedDown(), rangeStart,
							needContent);
					} else {
						previousRow = new TableRowModel(
								tagChild, idx, rangeStart, needContent);
					}
					rows.add(previousRow);
					if (needContent){
						content.addAll(previousRow.getContent());
					}
					idx++;
					rangeStart = previousRow.getEndOfRange() + 1;
				} else if (TABLE_SECTION_NAMES.contains(childName)){
					//we are inside "thead", "tbody" or "tfoot" tag
					previousRow = appendChildRows(
							tagChild, idx, previousRow, 
							rangeStart, needContent);
					idx = previousRow.getIndex();
					int prevRowRangeEnd = previousRow.getRange().getEnd();
					if (prevRowRangeEnd != Range.NOT_DEFINED){
						rangeStart = prevRowRangeEnd + 1;
					}
				} else {
					//TO DO: process col, colgroup and caption tags
				}
			}
		}
		return previousRow;
	}

	protected void populateColumns(boolean needContent){
		//figure out columns
		if (rows.size() > 0){
			//how many columns?
			int colCount = ((TableRowModel)rows.get(0)).getLengthInCols();
			//create them if don't exist
			if (columns == null){
				columns = new ArrayList<ICellSet>(colCount);
				for (int i = 0; i < colCount; i++){
					columns.add(new TableColumnModel(i, needContent));
				}
			} else {
				//match number of columns
				int change = colCount - columns.size();
				while (change != 0){
					if (change < 0){
						columns.remove(columns.size() - 1);
						change++;
					} else {
						columns.add(new TableColumnModel(
								columns.size(), needContent));
						change--;
					}
				}
				for (ICellSet column : columns){
					column.getCells().clear();
				}
			}
			//populate them
			for (ICellSet row : rows){
				int i = 0;
				for (TableCellModel cell : row){
					((TableColumnModel)columns.get(i)).appendCell(
							cell, needContent);
					i++;
				}
			}
		}
	}
	
	protected void updateColumnIndices(int startingWith){
		int columnCount = getColumnCount();
		this.checkColIdxBounds(startingWith);
		for (int i = startingWith; i < columnCount; i++){
			((TableColumnModel)getColumn(i)).setIndex(i);
		}
	}
	
	protected void appendRow(TableRowModel newRow){
		this.nullCheck(newRow);
		ArrayList<ICellSet> myRows = getRows();
		if (myRows == null){
			myRows = new ArrayList<ICellSet>();
			this.setRows(myRows);
		}
		newRow.setIndex(myRows.size());
		TagNode myTag = getTableTagNode();
		if (myTag != null){
			newRow.setTagParent(myTag);
			myTag.addChild(newRow.getRowTagNode());
		}
		myRows.add(newRow);
	}
	
	public void splitToInsertRow(int splitIdx){
		this.checkRowIdxBounds(splitIdx);
		//only need if not the first or last
		if (0 < splitIdx && splitIdx < getRowCount()){
			int prevIdx = splitIdx - 1;
			TableRowModel previousRow = 
				(TableRowModel)getRow(prevIdx);
			//only if there's something to split
			if (0 < previousRow.getMaxSpanDown()){
				TableRowModel nextRow = 
					(TableRowModel) getRow(splitIdx);
				List<TableCellModel> splitVictims = 
					previousRow.getSpannedDown();
				//this minRowIdx is to update maxSpanDown 
				//for all affected rows later
				int minRowIdx = previousRow.getIndex();
				for (TableCellModel splitVictim : splitVictims){
					minRowIdx = Math.min(minRowIdx, splitVictim.getRowIndex());
					int spanDownLength = 
						splitVictim.getSpanDownLength(prevIdx);
					TableCellModel emptySub = nextRow.substituteWithEmptyCell(
							splitVictim.getColIndex());
					for (int i = 1; i < spanDownLength; i++){
						((TableRowModel)getRow(splitIdx + i))
						   .substituteWith(emptySub);
					}
					splitVictim.changeRowSpan(-spanDownLength);
					//update columns?
					updateColumns(
							splitVictim.getColIndex(), 
							splitVictim.getColSpan(), 
							splitIdx, spanDownLength);
				}
				//update maxSpanDown for the rows above the split
				for (int i = minRowIdx; i < splitIdx; i++){
					((TableRowModel)getRow(i)).updateSpanDownInfo();
				}
			}
		}
	}
	
	protected void updateRowIndices(int startingWith){
		this.checkRowIdxBounds(startingWith);
		for (int i = startingWith; i < getRowCount(); i++){
			((TableRowModel)getRow(i)).setIndex(i);
		}
	}
	
	/**
	 * this sets content of the columns to null. Call updateContent() on them if you
	 * with to keep that in-sync
	 * @param startWith
	 * @param amount
	 * @param fromRow
	 */
	protected void updateColumns(int startWith, int amount, int fromRow, int length){
		this.checkColIdxBounds(startWith);
		int notIncluding = startWith + amount;
		this.checkColIdxBounds(notIncluding - 1);
		this.checkRowIdxBounds(fromRow);
		int toRow = fromRow + length;
		this.checkRowIdxBounds(toRow - 1);
		for (int i = startWith; i < notIncluding; i++){
			TableColumnModel col = (TableColumnModel)getColumn(i);
			col.removeCells(fromRow, length);
			for(int j = fromRow; j < toRow; j++){
				ICellSet row = getRow(j);
				col.addCell(j, row.getCell(i));
			}
		}
	}
	
	protected void checkColIdxBounds(int colIdx){
		int colCount = getColumnCount();
		if (colIdx < 0 || colCount <= colIdx){
			throw new IndexOutOfBoundsException(
					"Provided column index " + colIdx + " is out of bounds: " +
					"0 - " + (colCount - 1));
		}
	}
	
	protected void checkRowIdxBounds(int rowIdx){
		int rowCount = getRowCount();
		if (rowIdx < 0 || rowCount <= rowIdx){
			throw new IndexOutOfBoundsException(
					"Provided row index " + rowIdx + " is out of bounds: " +
					"0 - " + (rowCount - 1));
		}
	}
	
	protected void nullCheck(Object parameter){
		if (parameter == null){
			throw new IllegalArgumentException(
					"No null arguments are allowed");
		}
	}
}
