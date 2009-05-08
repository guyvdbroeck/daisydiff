package org.outerj.daisy.diff.html.dom.table;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeSet;

import org.outerj.daisy.diff.html.dom.Node;
import org.outerj.daisy.diff.html.dom.Range;
import org.outerj.daisy.diff.html.dom.TagNode;

import static org.outerj.daisy.diff.html.dom.table.TableStatics.*;

public class TableRowModel extends CellSetStub {

	//*-----------------------------------------------------------------------*
	//*                          Row Model itself                             *
	//*-----------------------------------------------------------------------*

	private TagNode rowTagNode = null;
	private Range rowRange = null;
	private int index = OUTSIDE;
	private ArrayList<TableCellModel> spannedDownCells = null;
	private int maxSpanDown = 0;
	private boolean empty = true;
	
	/**************************************************************************
	 * Creates a model from the row out of the <code>TagNode</code> tree.
	 * @param rowTag - row tag in the <code>TagNode</code> tree.
	 * @throws @see java.lang.IllegalArgumentException - if the 
	 * parameter is <code>null</code> or is not a row tag
	 */
	public TableRowModel(TagNode rowTag, int rowIndex, int rangeStart){
		if (rowTag == null || !ROW_TAG_NAME.equals(rowTag.getQName())){
			throw new IllegalArgumentException(
					"Can only build a row from not-null ROW TagNode");
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
								tagChild, index, idx, rangeStart);
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
						if (cellContent != null){
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
			List<TableCellModel> spannedCells, int rangeStart){
		if (rowTag == null || !ROW_TAG_NAME.equals(rowTag.getQName())){
			throw new IllegalArgumentException(
					"Can only build a row from not-null ROW TagNode");
		}
		
		if (spannedCells == null || spannedCells.size() == 0){
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
		content = new TreeSet<String>();
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
				
				//add its content to the row content once
				TreeSet<String> spannedContent = 
					spannedCell.getContent();
				if (spannedContent != null){
					content.addAll(spannedContent);
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
								tagChild, index, idx, rangeStart);
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
						if (cellContent != null){
							content.addAll(cellContent);
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
		DistinctCellIterator distinctCells = getDistinctIterator();
		while (distinctCells.hasNext()){
			TableCellModel myCell = distinctCells.next();
			//can't avoid tag copies, as some cells for a row are 
			//from other rows (spanned down)
			TableCellModel copy = myCell.copy();
			if (myCell.tagParentEquals(rowTagNode)){
				copy.setTagParent(rowTagCopy);
				rowTagCopy.addChild(myCell.getCellTagNode());
			}
			for (int i = 0; i < distinctCells.currentOccurence(); i++){
				cellsCopy.add(copy);
			}
		}
		rowCopy.setRowTagNode(rowTagCopy);
		rowCopy.setCells(cellsCopy);
		rowCopy.setContent(new TreeSet<String>(content));
		rowCopy.setRowRange(rowRange.copy());
		rowCopy.setIndex(OUTSIDE);
		rowCopy.setSpannedDownCells(
				new ArrayList<TableCellModel>(spannedDownCells));
		rowCopy.setMaxSpanDown(maxSpanDown);
		rowCopy.setEmpty(empty);
		return rowCopy;
	}
	
	public TableRowModel lightCopy(){
		TableRowModel rowCopy = new TableRowModel();
		TagNode rowTagCopy = (TagNode)rowTagNode.shallowCopy();
		ArrayList<TableCellModel> cellsCopy = new ArrayList<TableCellModel>();
		DistinctCellIterator distinctCells = getDistinctIterator();
		while (distinctCells.hasNext()){
			TableCellModel myCell = distinctCells.next();
			//can't avoid tag copies, as some cells for a row are 
			//from other rows (spanned down)
			TableCellModel copy = myCell.lightCopy();
			if (myCell.tagParentEquals(rowTagNode)){
				copy.setTagParent(rowTagCopy);
				rowTagCopy.addChild(myCell.getCellTagNode());
			}
			for (int i = 0; i < distinctCells.currentOccurence(); i++){
				cellsCopy.add(copy);
			}
		}
		rowCopy.setRowTagNode(rowTagCopy);
		rowCopy.setCells(cellsCopy);
		rowCopy.setEmpty(empty);
		return rowCopy;
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

	public int getMaxSpanDown(){
		return maxSpanDown;
	}
	
	public List<TableCellModel> getSpannedDown(){
		return spannedDownCells;
	}
	
	public int getIndex(){
		return index;
	}
	
	public int getLengthInCols(){
		return cells.size();
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
	
	protected void setIndex(int value){
		this.index = (value < 0)? OUTSIDE: value;
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
}
