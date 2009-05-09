package org.outerj.daisy.diff.html.dom.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.outerj.daisy.diff.html.dom.Node;
import org.outerj.daisy.diff.html.dom.Range;
import org.outerj.daisy.diff.html.dom.TagNode;
import org.outerj.daisy.diff.html.dom.TextNode;
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
		if (tableTag == null || !TABLE_TAG_NAME.equals(tableTag.getQName())) {
			throw new IllegalArgumentException(
					"Can only build a table from not-null TABLE TagNode");
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
			content = new TreeSet<String>();
			int idx = 0;
			//figure out rows
			appendChildRows(tableTagNode, idx, null, rangeIdx);
			populateColumns();
		}
	}

	public boolean hasCommonContentWith(TableModel another){
		if (another == null){
			throw new IllegalArgumentException(
					"No null arguments allowed");
		}
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
	
	public TableModel lightCopy(){
		TableModel tableCopy = new TableModel();
		Range myRange = getRange();
		if (myRange != null){
			tableCopy.setRange(myRange.copy());
		}
		TagNode myTag = getTableTagNode();
		if (myTag != null){
			tableCopy.setTableTagNode(myTag);
		}
		if (0 < getRowCount()){
			Iterator<ICellSet> myRows = getRows().iterator();
			TableRowModel rowCopy = null; 
			while (myRows.hasNext()){
				TableRowModel myRow = (TableRowModel)myRows.next();
				rowCopy = myRow.lightCopy(rowCopy);
				tableCopy.appendRow(rowCopy);
			}
		}
		tableCopy.populateColumns();
		return tableCopy;
	}
	
	public TableModel copyRangeOfRows(int startIdx, int amount){
		int notIncludedEnd = startIdx + amount;
		if (startIdx < 0 || amount <= 0 ||
			this.getRowCount() < notIncludedEnd){
			throw new IndexOutOfBoundsException(
					"The table has " + this.getRowCount() + " rows, " +
					"but rows " + startIdx + " - " + (notIncludedEnd - 1) +
					" were requested.");
		}
		TableModel rowExtract = new TableModel();
		TableRowModel rowCopy = null;
		for (int i = startIdx; i < notIncludedEnd; i++){
			rowCopy = ((TableRowModel)getRow(i)).lightCopy(rowCopy);
			rowExtract.appendRow(rowCopy);
		}
		Range extractRange = new Range(
				((TableRowModel)rowExtract.getRow(0)).getRange().getStart(),
				((TableRowModel)rowExtract.getLastRow()).getRange().getEnd());
		rowExtract.setRange(extractRange);
		rowExtract.populateColumns();
		return rowExtract;
	}
	
	public TableRowModel getLightCopyOfRow(int idx){
		if (idx < 0 || this.getRowCount() <= idx){
				throw new IndexOutOfBoundsException(
						"The table has " + this.getRowCount() + " rows, " +
						"but row" + idx + " was requested.");
		}
		return this.getRowCopy(idx).lightCopy();
	}
	
	/**
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
	public void insertEmptyColumns(int startIdx, int amount){
		if (startIdx < 0 || getColumnCount() < startIdx){
			throw new IndexOutOfBoundsException(
					"Currently index could be from 0 to " +
					getColumnCount() + ", but " + startIdx + 
					" was provided.");
		}
		//Create column models for the inserted columns
		List<TableColumnModel> newCols = new ArrayList<TableColumnModel>();
		for (int i = 0; i < amount; i++){
			TableColumnModel newCol = new TableColumnModel(startIdx + i);
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
				rowModel.updateCellColIdx();
				spanned--;
			}
			for (int i = 0; i < amount; i++){
				newCols.get(i).appendCell(rowModel.getCell(startIdx + i));
			}
		}
		
		//updateColumns
		//a).insert the new column models
		this.getColumns().addAll(startIdx, newCols);
		
		//b). Update indices for the tail
		this.updateColumnIndices(startIdx + amount);
	}
	
	/**
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
		//update indices and columns
		
		//TO DO: finish
	}
	
	public void insertRowRange(TableModel rowRange, int startIdx){
		//TO DO: finish
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
		if (idx < 0 || getRowCount() <= idx){
			return null;
		} else {
			return rows.get(idx);
		}
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
		if (idx < 0 || getRowCount() <= idx){
			return null;
		} else {
			return ((TableRowModel)rows.get(idx)).copy();
		}
	}
	
	public List<ICellSet> getColumns(){
		return columns;
	}
	
	public ICellSet getColumn(int idx){
		if (idx < 0 || getColumnCount() <= idx){
			return null;
		} else {
			return columns.get(idx);
		}
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
		if (tableTag != null && 
			!TABLE_TAG_NAME.equals(tableTag.getQName().toLowerCase())){
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
			TableRowModel previousRow, int rangeStart){
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
							previousRow.getSpannedDown(), rangeStart);
					} else {
						previousRow = new TableRowModel(
								tagChild, idx, rangeStart);
					}
					rows.add(previousRow);
					content.addAll(previousRow.getContent());
					idx++;
					rangeStart = previousRow.getEndOfRange() + 1;
				} else if (TABLE_SECTION_NAMES.contains(childName)){
					//we are inside "thead", "tbody" or "tfoot" tag
					previousRow = 
						appendChildRows(tagChild, idx, previousRow, rangeStart);
					idx = previousRow.getIndex();
					rangeStart = previousRow.getRange().getEnd() + 1;
				} else {
					//TO DO: process col, colgroup and caption tags
				}
			}
		}
		return previousRow;
	}

	protected void populateColumns(){
		//figure out columns
		if (rows.size() > 0){
			//how many columns?
			int colCount = ((TableRowModel)rows.get(0)).getLengthInCols();
			//create them if don't exist
			if (columns == null){
				columns = new ArrayList<ICellSet>(colCount);
				for (int i = 0; i < colCount; i++){
					columns.add(new TableColumnModel(i));
				}
			} else {
				//match number of columns
				int change = colCount - columns.size();
				while (change != 0){
					if (change < 0){
						columns.remove(columns.size() - 1);
						change++;
					} else {
						columns.add(new TableColumnModel(columns.size()));
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
					((TableColumnModel)columns.get(i)).appendCell(cell);
					i++;
				}
			}
		}
	}
	
	protected void updateColumnIndices(int startingWith){
		int columnCount = getColumnCount();
		if (startingWith < 0 || columnCount < startingWith){
			throw new IndexOutOfBoundsException(
					"Currently index could be from 0 to " +
					columnCount + ", but " + startingWith + 
					" was provided.");
		}
		for (int i = startingWith; i < columnCount; i++){
			((TableColumnModel)getColumn(i)).setIndex(i);
		}
	}
	
	protected void appendRow(TableRowModel newRow){
		if (newRow == null){
			throw new IllegalArgumentException(
					"No null parameters allowed");
		}
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
		if (splitIdx < 0 || getRowCount() < splitIdx){
			throw new IndexOutOfBoundsException(
					"Currently index could be from 0 to " +
					getRowCount() + ", but " + splitIdx + 
					" was provided.");
		}
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
					splitVictim.changeRowSpan(-spanDownLength);
					TableCellModel emptyCell = new TableCellModel(
							getEmptyCell(), 
							splitIdx, splitVictim.getColIndex(), 
							Range.NOT_DEFINED);
					emptyCell.changeRowSpan(spanDownLength);
					for (int i = 0; i < spanDownLength; i++){
						((TableRowModel)getRow(splitIdx + i))
						   .substituteCell(emptyCell);
					}
					//update columns
				}
			}
		}
		
	}
}
