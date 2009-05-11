package org.outerj.daisy.diff.html.dom.table;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeSet;

import org.outerj.daisy.diff.html.dom.Node;
import org.outerj.daisy.diff.html.dom.Range;
import org.outerj.daisy.diff.html.dom.TagNode;
import org.outerj.daisy.diff.html.modification.ModificationType;

import static org.outerj.daisy.diff.html.dom.table.TableStatics.*;

public class TableRowModel extends CellSetStub {

	//*-----------------------------------------------------------------------*
	//*                          Row Model itself                             *
	//*-----------------------------------------------------------------------*

	private TagNode rowTagNode = null;
	private Range rowRange = null;
	private int index = OUTSIDE;
	private ArrayList<TableCellModel> spannedDownCells = null;
	
	//maxSpanDown is the number of rows below this row that shares cells with 
	//this row
	private int maxSpanDown = 0;
	private boolean empty = true;
	
	/**************************************************************************
	 * Creates a model from the row out of the <code>TagNode</code> tree.
	 * @param rowTag - row tag in the <code>TagNode</code> tree.
	 * @throws @see java.lang.IllegalArgumentException - if the 
	 * parameter is <code>null</code> or is not a row tag
	 */
	public TableRowModel(
			TagNode rowTag, int rowIndex, int rangeStart, boolean needContent){
		this.nullCheck(rowTag);
		if (!ROW_TAG_NAME.equals(rowTag.getQName())){
			throw new IllegalArgumentException(
					"Can only build a row from a ROW TagNode");
		}
		//remember where we came from 
		rowTagNode = rowTag;
		rowRange = new Range(rangeStart);
		
		//remember our place in the table
		index = (rowIndex < 0)? OUTSIDE : rowIndex;
		
		//we are interested in td/th tags
		if (rowTagNode.getNbChildren() > 0){
			cells = new ArrayList<TableCellModel>();
			content = new TreeSet<String>();
			int idx = 0;
			for (Node child : rowTagNode){
				if (child != null && child instanceof TagNode){
					TagNode tagChild = (TagNode)child;
					String childName = tagChild.getQName();
					if (CELL_TAG_NAME.equals(childName) || 
						HEADER_CELL_TAG_NAME.equals(childName)){
						//it's a cell tag!
						TableCellModel cell = new TableCellModel(
								tagChild, index, idx, rangeStart, needContent);
						rangeStart = cell.getEndOfRange();
						rowRange.setEnd(rangeStart++);
						//if the cell spans several rows, then 
						//it will be omitted in the following
						//rows, so we must take a note.
						int height = cell.getRowSpan();
						if (height > NO_SPAN){
							if (spannedDownCells == null){
								spannedDownCells = 
									new ArrayList<TableCellModel>();
							}
							spannedDownCells.add(cell);
							//after that only need remaining span
						}
						height--;//subtracted current row
						if (height > maxSpanDown){
							maxSpanDown = height;
						}
						
						//insert that cell into every column it belongs to.
						int width = cell.getColSpan();
						for (int i = 0; i < width; i++){
							cells.add(cell);
							idx++;
						}
						
						//add the cell's content to the row's content.
						//note, that because the content is a set, there
						//will be no duplicates. Because it's a sorted set,
						//the insertion and the look up are sped up a bit.
						TreeSet<String> cellContent = cell.getContent();
						if (needContent && cellContent != null){
							content.addAll(cell.getContent());
						}
						if (empty && !cell.isEmpty()){
							empty = false;
						}
					} else {
						//child of a table row isn't a cell? some kind of mistake
						System.out.println("ERROR in HTML table code: " +
								"table row tag has a child that is not " +
								"a table cell! (" + childName + ")");
					}
				} else {
					//child of a table row is not a TagNode?! should be a cell tag!
					System.out.println("ERROR in HTML table code: " +
							"table row tag has a child that is not " +
							"an HTML tag! (" + child.toString() + ")");
				}
			}
		}
	}
	
	/**************************************************************************
	 * Creates a model from the row out of the <code>TagNode</code> tree.
	 * @param rowTag - row tag in the <code>TagNode</code> tree.
	 * @param spannedCells - list of cells from previous row, that are spanned
	 * onto this row.
	 * @throws @see java.lang.IllegalArgumentException - if the 
	 * parameter is <code>null</code> or is not a row tag
	 */
	public TableRowModel(
			TagNode rowTag, int rowIndex, 
			List<TableCellModel> spannedCells, int rangeStart, 
			boolean needContent){
		this.nullCheck(rowTag);
		this.nullCheck(spannedCells);
		if (!ROW_TAG_NAME.equals(rowTag.getQName())){
			throw new IllegalArgumentException(
					"Can only build a row from a ROW TagNode");
		}
		
		if (spannedCells.size() == 0){
			throw new IllegalArgumentException(
					"Invalid list of spanned cells");
		}

		//remember where we came from 
		rowTagNode = rowTag;
		rowRange = new Range(rangeStart);

		//remember our place in the table
		index = (rowIndex < 0)? OUTSIDE : rowIndex;
		
		//try to build row from current children and spanned cells
		cells = new ArrayList<TableCellModel>();
		if (needContent){
			content = new TreeSet<String>();
		}
		Iterator<TableCellModel> talls = spannedCells.iterator();
		boolean ownChildren = rowTagNode.getNbChildren() > 0;
		Iterator<Node> children = null;
		if (ownChildren){
			children = rowTagNode.iterator();
		}
		int idx = 0;
		int nextTallIdx = OUTSIDE;
		TableCellModel spannedCell = null;
		
		//start with a check for spanned cells
		//as they know their place in the table
		if (talls.hasNext()){
			spannedCell = talls.next();
			nextTallIdx = spannedCell.getColIndex();
		}
		
		while (children.hasNext() || nextTallIdx != OUTSIDE){
			//get all consecutive spanned cells 
			//that should be in these columns
			while(idx == nextTallIdx){
				//insert that cell into this row (how many times?)
				
				for (int colSpan = spannedCell.getColSpan(), 
					 i = 0; 
				     i < colSpan; i++){
					cells.add(spannedCell);
					//update idx
					idx++;
				}
				
				//if we need content - add its content to the row content once
				if (needContent){
					TreeSet<String> spannedContent = 
						spannedCell.getContent();
					if (spannedContent != null){
						content.addAll(spannedContent);
					}
				}
				//will that cell span below this row?
				int height = spannedCell.getRowSpan();
				int topRowIdx = spannedCell.getRowIndex();
				int remainingSpan = topRowIdx + height - index;
				if (remainingSpan > NO_SPAN){
					//then it will span below
					if (spannedDownCells == null){
						spannedDownCells = new ArrayList<TableCellModel>();
					}
					spannedDownCells.add(spannedCell);
					remainingSpan--; //subtract the current row
					if (remainingSpan > maxSpanDown){
						maxSpanDown = remainingSpan;
					}
				}
				
				//move to the next spanned cell if there's one
				if (talls.hasNext()){
					spannedCell = talls.next();
					nextTallIdx = spannedCell.getColIndex();
				} else {
					spannedCell = null;
					nextTallIdx = OUTSIDE;
				}
			}
			
			//either spanned cell are all inserted or 
			//there is some children of our own in between
			//insert own kids while possible
			//interested in td/th tags
			while (children.hasNext() && idx != nextTallIdx){
				Node child = children.next();
				if (child != null && child instanceof TagNode){
					TagNode tagChild = (TagNode)child;
					String childName = tagChild.getQName();
					if (CELL_TAG_NAME.equals(childName) || 
						HEADER_CELL_TAG_NAME.equals(childName)){
						//it's a cell tag!
						TableCellModel cell = new TableCellModel(
								tagChild, index, idx, rangeStart, needContent);
						rangeStart = cell.getEndOfRange();
						rowRange.setEnd(rangeStart++);
						
						//if the cell spans several rows, then 
						//it will be omitted in the following
						//rows, so we must take a note.
						int height = cell.getRowSpan();
						if (height > NO_SPAN){
							if (spannedDownCells == null){
								spannedDownCells = 
									new ArrayList<TableCellModel>();
							}
							spannedDownCells.add(cell);
							//after that only need remaining span
						}
						height--;//subtracted current row
						if (height > maxSpanDown){
							maxSpanDown = height;
						}
						
						//insert that cell into every column it belongs to.
						int width = cell.getColSpan();
						for (int i = 0; i < width; i++){
							cells.add(cell);
							idx++;
						}
							
						if (needContent){
							//add the cell's content to the row's content.
							//note, that because the content is a set, there
							//will be no duplicates. Because it's a sorted set,
							//the insertion and the look up are sped up a bit.
							TreeSet<String> cellContent = cell.getContent();
							if (cellContent != null){
								content.addAll(cellContent);
							}
						}
					} else {
						//child of a table row isn't a cell? 
						//some kind of mistake
						System.out.println("ERROR in HTML table code: " +
								"table row tag has a child that is not " +
								"a table cell! (" + childName + ")");
					}
				} else {
					//child of a table row is not a TagNode?! 
					//should be a cell tag!
					System.out.println("ERROR in HTML table code: " +
							"table row tag has a child that is not " +
							"an HTML tag! (" + child.toString() + ")");
				}
			}//end of checking children
			//when we exit own children loop, we either done, 
			//or need to repeat alteration between spanned cells and own kids
		}
	}
	
	protected TableRowModel(){
		//doing nothing
	}
	
	public TableRowModel copy(){
		TableRowModel rowCopy = new TableRowModel();
		TagNode rowTagCopy = (TagNode)rowTagNode.shallowCopy();
		ArrayList<TableCellModel> cellsCopy = new ArrayList<TableCellModel>();
		ArrayList<TableCellModel> spannedDownCopy = 
			new ArrayList<TableCellModel>();
		DistinctCellIterator distinctCells = getDistinctIterator();
		while (distinctCells.hasNext()){
			TableCellModel myCell = distinctCells.next();
			//can't avoid tag copies, as some cells for a row are 
			//from other rows (spanned down)
			TableCellModel copy = myCell.copy();
			if (myCell.tagParentEquals(rowTagNode)){
				copy.setTagParent(rowTagCopy);
				rowTagCopy.addChild(copy.getCellTagNode());
			} else {//means it's spanned from previous row
				//we need to adjust spans to not span into the previous rows
				copy.setRowSpan(
						myCell.getRowSpan() - 
						(getIndex() - myCell.getRowIndex()));
			}
			//spanned down list should contain cell from the row, 
			//so we can't just make a copy - we need to extract our cells.
			if (NO_SPAN < copy.getRowSpan()){
				spannedDownCopy.add(copy);
			}
			//adjust columnIdx
			copy.setColIndex(myCell.getColIndex());
			//add this cell as many times as it spans columns
			for (int i = 0; i < distinctCells.currentOccurence(); i++){
				cellsCopy.add(copy);
			}
		}
		rowCopy.setRowTagNode(rowTagCopy);
		rowCopy.setCells(cellsCopy);
		rowCopy.setSpannedDownCells(spannedDownCopy);
		if (hasContent()){
			rowCopy.setContent(new TreeSet<String>(content));
		}
		Range myRange = this.getRange();
		if (myRange != null){
			rowCopy.setRowRange(myRange.copy());
		} else {
			rowCopy.setRowRange(null);
		}
		rowCopy.setIndex(OUTSIDE);
		rowCopy.setMaxSpanDown(maxSpanDown);
		rowCopy.setEmpty(empty);
		return rowCopy;
	}
	
	/**************************************************************************
	 * This method adjust rowspans of the cells in the row to not span
	 * in the previous row. It also makes all the cells to be children
	 * of the row. Therefore, if you want to copy a series of rows without 
	 * breaking rowspanned cells, you can't repeatedly use this method.
	 * Instead use <code>lightCopy(TableRowModel previousRow)</code>method.
	 * @return
	 */
	public TableRowModel lightCopy(int maxAllowedSpanDown){
		TableRowModel rowCopy = new TableRowModel();
		TagNode rowTagCopy = (TagNode)rowTagNode.shallowCopy();
		ArrayList<TableCellModel> cellsCopy = new ArrayList<TableCellModel>();
		ArrayList<TableCellModel> spannedDownCopy = 
			new ArrayList<TableCellModel>();
		int rowIdx = getIndex();
		int maxCopySpanDown = 0;
		DistinctCellIterator distinctCells = getDistinctIterator();
		while (distinctCells.hasNext()){
			TableCellModel myCell = distinctCells.next();
			//can't avoid separate tag copies, as some cells for a row are 
			//from other rows (spanned down)
			TableCellModel copy = myCell.lightCopy();
			int originalRowSpan = myCell.getRowSpan();
			int copyRowSpan = copy.getRowSpan();
			if (!myCell.tagParentEquals(rowTagNode)){
				//means it's spanned from previous row
				//we need to adjust spans to not span into the previous rows
				copyRowSpan += myCell.getRowIndex() - rowIdx;
			}
			copy.setTagParent(rowTagCopy);
			rowTagCopy.addChild(copy.getCellTagNode());
			//spanned down list should contain cell from the row, 
			//so we can't just make a copy - we need to extract our cells.
			//we also need to adjust that to not extend past copying piece
			if (maxAllowedSpanDown < (copyRowSpan - 1)){
				copyRowSpan = maxAllowedSpanDown + 1;
			}
			copy.changeRowSpan(originalRowSpan - copyRowSpan);
			if (NO_SPAN < copyRowSpan){
				spannedDownCopy.add(copy);
				maxCopySpanDown = Math.max(maxCopySpanDown, copyRowSpan - 1);
			}
			//adjust columnIdx
			copy.setColIndex(myCell.getColIndex());
			//add this cell as many times as it spans columns
			for (int i = 0; i < distinctCells.currentOccurence(); i++){
				cellsCopy.add(copy);
			}
		}
		rowCopy.setRowTagNode(rowTagCopy);
		rowCopy.setCells(cellsCopy);
		rowCopy.setSpannedDownCells(spannedDownCopy);
		rowCopy.setMaxSpanDown(maxCopySpanDown);
		rowCopy.setEmpty(empty);
		return rowCopy;
	}
	
	/**************************************************************************
	 * This method allows to share spanned cell if you want to make a copy of 
	 * several rows with spanned cell and use them together.
	 * @param previousRow - previous row (probably with spanned cells
	 * @param maxAllowedSpanDown - how far the cells of the copy row can 
	 * span down
	 * @return the copy
	 */
	public TableRowModel lightCopy(
			TableRowModel previousRow, int maxAllowedSpanDown){
		if (previousRow == null){
			return lightCopy(maxAllowedSpanDown);
		}
		int	copyIdx = previousRow.getIndex() + 1;
		TableRowModel rowCopy = new TableRowModel();
		rowCopy.setIndex(copyIdx);
		TagNode rowTagCopy = (TagNode)rowTagNode.shallowCopy();
		ArrayList<TableCellModel> cellsCopy = new ArrayList<TableCellModel>();
		ArrayList<TableCellModel> spannedDownCopy = 
			new ArrayList<TableCellModel>();
		int maxCopySpanDown = 0;
		DistinctCellIterator distinctCells = getDistinctIterator();
		while (distinctCells.hasNext()){
			TableCellModel myCell = distinctCells.next();
			//can't avoid tag copies, as some cells for a row are 
			//from other rows (spanned down)
			TableCellModel copy;
			if (myCell.tagParentEquals(rowTagNode)){
				copy = myCell.lightCopy();
				copy.setTagParent(rowTagCopy);
				rowTagCopy.addChild(copy.getCellTagNode());
				//adjust columnIdx and rowIdx
				copy.setColIndex(myCell.getColIndex());
				copy.setRowIndex(copyIdx);
				//adjust span down
				int copyRowSpan = copy.getRowSpan();
				if (maxAllowedSpanDown < (copyRowSpan - 1)){
					copy.changeRowSpan(maxAllowedSpanDown - copyRowSpan + 1);
				}
			} else {//means it's spanned from previous row
				//get it from the previous row
				copy = previousRow.getCell(myCell.getColIndex());
			}
			//spanned down list should contain cell from the row, 
			//so we can't just make a copy - we need to extract our cells.
			int copySpanDown = copy.getSpanDownLength(copyIdx);
			if (0 < copySpanDown){
				spannedDownCopy.add(copy);
				maxCopySpanDown = Math.max(maxCopySpanDown, copySpanDown);
			}
			//add this cell as many times as it spans columns
			for (int i = 0; i < distinctCells.currentOccurence(); i++){
				cellsCopy.add(copy);
			}
		}
		rowCopy.setRowTagNode(rowTagCopy);
		rowCopy.setCells(cellsCopy);
		rowCopy.setSpannedDownCells(spannedDownCopy);
		rowCopy.setMaxSpanDown(maxCopySpanDown);
		rowCopy.setEmpty(empty);
		return rowCopy;
	}
	
	public void mark(ModificationType kind){
		this.setModification(kind);
		for (TableCellModel cell : this){
			cell.setModification(kind);
		}
	}
	
	protected boolean insertedEmptyCells(int startIdx, int amount){
		int cellCount = getLengthInCols();
		if (startIdx < 0 || cellCount < startIdx){
			throw new IndexOutOfBoundsException(
					"Currently index could be from 0 to " +
					cellCount);
		}
		TagNode myTag = getRowTagNode();
		int prevIdx = startIdx - 1;
		if (0 < startIdx){
			TableCellModel previousCell = this.getCell(prevIdx);
			//check if the cell in front spans columns - 
			//then we will not insert the empty cells
			if (NO_SPAN < previousCell.getColSpan() - 
					         (startIdx - 1 - previousCell.getColIndex())){
				return false;
			}
			//find idx of previous OWN child to insert tag later
			//OWN child vs cell that belongs to some row ahead and
			//just spanned down
			while(!previousCell.tagParentEquals(myTag) && 0 <= prevIdx){
				previousCell = this.getCell(prevIdx);
				prevIdx = previousCell.getColIndex() - 1;
			}
		}
		//all's good - insert empty cells
		for (int i = startIdx; i < startIdx + amount; i++){
			TableCellModel emptyCell = new TableCellModel(
					getEmptyCell(), getIndex(), i, Range.NOT_DEFINED, false);
			emptyCell.setTagParent(myTag);
			if (myTag != null){
				myTag.addChild(prevIdx + 1, emptyCell.getCellTagNode());
				prevIdx++;
			}
		}
		return true;
	}
	
	/**
	 * This method increases or decreases the row length (in columns) 
	 * by increasing/decreasing colspan for the cell at the specified index.
	 * It should be used very carefully, only by the table model, to adjust
	 * other rows accordingly. Notice, that spanned cells are shared
	 * among different rows/columns, which increase the danger of this method.
	 * @param colIdx - index of the cell which span we change
	 * @param spanChange - amount that will be added to the current span.
	 * If the resulted span is 0 or less, it will be adjusted to be 1.
	 * @return the cell if the change happened and <code>null</code> otherwise
	 * The returned cell can be the key to updating all affected rows.
	 * @throws <code>IndexOutOfBoundsException</code>
	 * if colIdx is outside of cells collection bounds
	 */
	protected TableCellModel changeCellColSpan(int colIdx, int spanChange){
		int cellCount = getLengthInCols();
		if (colIdx < 0 || cellCount <= colIdx){
			throw new IndexOutOfBoundsException(
					"Currently index should be from 0 to " + (cellCount - 1));
		}
		TableCellModel theCell = getCell(colIdx);
		//adjusted span change
		spanChange = theCell.changeColSpan(spanChange);
		if (spanChange == 0){
			return null;
		}
		//in the beginning of this cell, add or delete this cell
		//so the amount of cells in the collection would match
		//amount of columns
		colIdx = theCell.getColIndex();
		List<TableCellModel> myCells = getCells();
		while (spanChange != 0){
			if (spanChange > 0){
				//means we should add more of this cell
				myCells.add(colIdx, theCell);
				spanChange--;
			} else if (spanChange < 0){
				//means we should remove the "extras"
				myCells.remove(colIdx);
				spanChange++;
			}
		}
		updateCellColIdx();
		return theCell;
	}
	
	/**
	 * this method should be used if a cell from a row above spans over this row,
	 * and had its colspan increased by the row above. Now this row has to add the 
	 * spanned cell to its cell collection to match the length increase.
	 * @param idx - index of the spanned row
	 * @param amount - additional columns that now spanned
	 */
	protected void updateWithSpannedCell(int idx, int amount){
		this.checkIdx(idx);
		if (amount <= 0){//don't handle negative changes 
			return;
		}
		TableCellModel spannedCell = getCell(idx);
		List<TableCellModel> myCells = getCells();
		if (NO_SPAN < spannedCell.getColSpan()){
			while (amount != 0){
				myCells.add(idx, spannedCell);
				amount--;
			}
			updateCellColIdx();
		}
	}

	/**
	 * This method substitutes the cell in the specified column with an empty cell
	 * that doesn't span up, but spans down  and over the columns exactly same amount
	 * or rows/columns as the original cell in that spot.
	 * This method does not update the table's other affected rows and columns - it is 
	 * the responsibility of the caller. Currently this method is used for 
	 * splitting the cells that span over the insertion point.
	 * @param colIdx - the index of the cell that needs to be substituted.
	 * @throws <code>IndexOutOfBoundsException</code>
	 */
	protected TableCellModel substituteWithEmptyCell(int colIdx){
		int colCount = getLengthInCols();
		if (colIdx < 0 || colCount <= colIdx){
			throw new IndexOutOfBoundsException(
					"The provided substitution has to have valid colIndex." +
					"Currently index could be from 0 to " + colCount +
					", but " + colIdx + " was provided. ");
		}
		TableCellModel oldCell = getCell(colIdx);
		int rowIdx = getIndex();
		int colSpan = oldCell.getColSpan();
		TableCellModel emptyCell = new TableCellModel(
				getEmptyCell(), 
				rowIdx, colIdx, 
				Range.NOT_DEFINED, false);
		emptyCell.changeRowSpan(oldCell.getSpanDownLength(rowIdx) + 1);
		emptyCell.changeColSpan(colSpan - emptyCell.getColSpan());
		//take care of the tag tree if needed
		TagNode myTag = getRowTagNode();
		if (myTag!= null){
			emptyCell.setTagParent(myTag);
			//the following is not going to happen during the split, 
			//(as the old cell comes from the rows above.
			//but to be on the safe side...
			if (oldCell.getRowIndex() == rowIdx){
				TagNode oldCellTag = oldCell.getCellTagNode();
				myTag.removeChild(oldCellTag);
				oldCellTag.setParent(null);
			}
			//insert it in the right spot
			int prevColIdx = colIdx - 1;
			while (0 <= prevColIdx && getCell(prevColIdx).getRowIndex() != rowIdx){
				prevColIdx--;
			}
			if (prevColIdx == OUTSIDE){//means no own children before substitution 
				//insert first
				myTag.addChild(0, emptyCell.getCellTagNode());
			} else { //we found previous own child - insert after it.
				myTag.addChild(
					myTag.getIndexOf(getCell(prevColIdx).getCellTagNode()) + 1,
					emptyCell.getCellTagNode());
			}
		}
		//now do the model replacement as many times as colspan says
		LinkedList<TableCellModel> substitution = new LinkedList<TableCellModel>();
		List<TableCellModel> myCells = getCells();
		for(int i = 0; i < colSpan; i++){
			myCells.remove(colIdx);
			substitution.add(emptyCell);
		}
		myCells.addAll(colIdx, substitution);
		return emptyCell;
	}
	
	/**
	 * Substitution should have same colIdx, colSpan and spanDown as the current cell.
	 * Currently this method assumes that the substituted cell is not the own child
	 * of this row, but is spanned down from the previous row.
	 * @param substitution
	 */
	protected void substituteWith(TableCellModel substitution){
		int colIdx = substitution.getColIndex();
		int colCount = getLengthInCols();
		int rowIdx = getIndex();
		if (colIdx < 0 || colCount <= colIdx){
			throw new IndexOutOfBoundsException(
					"current row can't accomodate requested coordinates: " +
					"requested colIdx: " + colIdx + 
					" possible values: from 0 to " + colCount);
		}
		TableCellModel oldCell = getCell(colIdx);
		int oldColSpan = oldCell.getColSpan();
		int oldSpanDown = oldCell.getSpanDownLength(rowIdx);
		int subColSpan = substitution.getColSpan();
		int subSpanDown = substitution.getSpanDownLength(rowIdx);
		if (subColSpan != oldColSpan || subSpanDown != oldSpanDown){
			throw new IllegalStateException(
				"provided substitution should have same " +
				"colSpan and spanDown values as the old cell.\n" +
				"Old values: " + oldColSpan + ", " + oldSpanDown + "\n" +
				"New values: " + subColSpan + ", " + subSpanDown);
		}
		//substitute as many times as needed
		LinkedList<TableCellModel> subPiece = new LinkedList<TableCellModel>();
		List<TableCellModel> myCells = getCells();
		for(int i = 0; i < oldColSpan; i++){
			myCells.remove(colIdx);
			subPiece.add(substitution);
		}
		myCells.addAll(colIdx, subPiece);
	}
	
	protected void updateSpanDownInfo(){
		int currentMaxSpanDown = 0;
		int rowIdx = getIndex();
		List<TableCellModel> rowSpannedCells = getSpannedDown();
		//the list will be changing, so we can't use iterator, and have to move 
		//from end to front
		int originalCount = rowSpannedCells.size();
		for (int i = originalCount - 1; i >= 0; i-- ){
			TableCellModel cellToCheck = rowSpannedCells.get(i);
			int spanDown = cellToCheck.getSpanDownLength(rowIdx);
			if (spanDown == 0){
				rowSpannedCells.remove(i);
			} else {
				currentMaxSpanDown = Math.max(currentMaxSpanDown, spanDown);
			}
		}
		this.setMaxSpanDown(currentMaxSpanDown);
	}
	
	//*-----------------------------------------------------------------------*
	//*                  CellSet interface implementation                     *
	//*-----------------------------------------------------------------------*
	@Override
	public boolean isEmpty(){
		return empty;
	}
	
	@Override
	public Iterator<TableCellModel> iterator(){
		return cells.iterator();
	}
	
	public DistinctCellIterator getDistinctIterator(){
		return new DistinctCellIterator(getCells());
	}
	
	//*-----------------------------------------------------------------------*
	//*                           getters/setters                             *
	//*-----------------------------------------------------------------------*

	/**
	 * If this row doesn't contain cells that span over to the next row, 
	 * then this method returns 0.<br>
	 * If this row contains cells with rowspan attributes (even if in HTML those
	 * cells belong to the row above), then this method returns <b>maximum</b>
	 * of the following expressions for each of its cells:
	 * <code>
	 *   aCell.getRowSpan() - (this.getIndex() - aCell.getRowIndex()) - 1
	 * </code>
	 * @returns number of rows below this row that share cells with this row.
	 * 
	 */
	public int getMaxSpanDown(){
		return maxSpanDown;
	}
	
	public List<TableCellModel> getSpannedDown(){
		return spannedDownCells;
	}
	
	public boolean hasSpannedDown(){
		return (spannedDownCells != null &&
				spannedDownCells.size() > 0 &&
				maxSpanDown > 0);
	}
	
	public int getIndex(){
		return index;
	}
	
	public int getLengthInCols(){
		if (cells == null){
			return 0;
		} else {
			return cells.size();
		}
	}
	
	public Range getRange(){
		return rowRange;
	}
	
	public int getEndOfRange(){
		if (rowRange == null){
			return Range.NOT_DEFINED;
		} else {
			return rowRange.getEnd();
		}
	}
	
	public TableCellModel getCell(int index){
		return getCells().get(index);
	}
	
	public TagNode getRowTagNode(){
		return rowTagNode;
	}
	
	protected void setCells(ArrayList<TableCellModel> cells){
		this.cells = cells;
	}
	
	protected void setContent(TreeSet<String> content){
		this.content = content;
	}
	
	protected void setRowTagNode(TagNode rowTagNode){
		if (rowTagNode == null || 
			!ROW_TAG_NAME.equals(rowTagNode.getQName())){
			throw new IllegalArgumentException(
			"Not-null ROW TagNode only");
		}
		this.rowTagNode = rowTagNode;
	}
	
	protected void setRowRange(Range rowRange){
		this.rowRange = rowRange;
	}
	
	public void setIndex(int value){
		int oldValue = this.getIndex();
		this.index = (value < 0)? OUTSIDE: value;
		//update own kids if any
		if (this.getLengthInCols() > 0){
			DistinctCellIterator distinctCells = this.getDistinctIterator();
			while(distinctCells.hasNext()){
				TableCellModel myCell = distinctCells.next();
				if (myCell.getRowIndex() == oldValue){
					myCell.setRowIndex(this.index);
				}
			}
		}
	}
	
	protected void setSpannedDownCells(
			ArrayList<TableCellModel> spannedDownCells){
		this.spannedDownCells = spannedDownCells;
	}
	
	protected void setMaxSpanDown(int maxSpanDown){
		this.maxSpanDown = (maxSpanDown < 0)? 0 : maxSpanDown;
	}
	
	protected void setEmpty(boolean empty){
		this.empty = empty;
	}
	
	public void setTagParent(TagNode newParent){
		if (newParent != null){
			String parentKind = newParent.getQName().toLowerCase();
			if (!TABLE_SECTION_NAMES.contains(parentKind) &&
				!TABLE_TAG_NAME.equals(parentKind)){
				throw new IllegalArgumentException(
					"Only table or table section can be the row tag parent");
			}
		}
		TagNode myTag = this.getRowTagNode();
		if (myTag == null){
			myTag = new TagNode(ROW_TAG_NAME);
			this.setRowTagNode(myTag);
		}
		myTag.setParent(newParent);
	}

	//*-----------------------------------------------------------------------*
	//*                         helper methods                                *
	//*-----------------------------------------------------------------------*

	protected void updateCellColIdx(){
		DistinctCellIterator distinctCells = getDistinctIterator();
		int currentColIdx = 0;
		while (distinctCells.hasNext()){
			TableCellModel currentCell = distinctCells.next();
			currentCell.setColIndex(currentColIdx);
			currentColIdx += currentCell.getColSpan();
		}
	}

	protected void checkIdx(int cellIdx){
		int colCount = this.getLengthInCols();
		if (cellIdx < 0 || colCount <= cellIdx){
			throw new IndexOutOfBoundsException(
					"Provided cell index " + cellIdx + " is out of bounds: " +
					"0 - " + (colCount - 1));
		}
	}

}
