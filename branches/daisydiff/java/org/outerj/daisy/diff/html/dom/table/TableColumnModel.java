package org.outerj.daisy.diff.html.dom.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.outerj.daisy.diff.html.modification.ModificationType;

import static org.outerj.daisy.diff.html.dom.table.TableStatics.*;

public class TableColumnModel extends CellSetStub{

	//*-----------------------------------------------------------------------*
	//*                        Column Model itself                            *
	//*-----------------------------------------------------------------------*

	private int index = OUTSIDE;
	private boolean empty = true;
	//content is only from non-spanned cells
	
	/**************************************************************************
	 * 
	 */
	public TableColumnModel(int columnIndex, boolean needContent){
		cells = new ArrayList<TableCellModel>();
		if (needContent){
			content = new TreeSet<String>();
		}
		index = (columnIndex < 0)? OUTSIDE : columnIndex;
	}
	
	public boolean appendCell(TableCellModel cell, boolean needContent){
		//find out the cell's desired place
		int rowIdx = cell.getRowIndex();
		int rowSpan = cell.getRowSpan();
		int colIdx = cell.getColIndex();
		int colSpan = cell.getColSpan();
		//are the coordinates correct?
		if ((cells.size() < rowIdx + rowSpan) && 
			colIdx <= index &&
			index < colIdx + colSpan){
			cells.add(cell);
			if (needContent && cell.getColSpan() == NO_SPAN){
				if (content == null){
					content = new TreeSet<String>();
				}
				TreeSet<String> cellContent = cell.getContent();
				if (cellContent != null){
					content.addAll(cellContent);
				}
			}
			if (empty && !cell.isEmpty()){
				empty = false;
			}
			return true;
		} else {
			return false;
		}
	}

	public void mark(ModificationType kind){
		this.setModification(kind);
		for (TableCellModel cell : this){
			if (NO_SPAN == cell.getColSpan()){
				cell.setModification(kind);
			}
		}
	}
	
	
	protected void removeCells(int startWithIdx, int amount){
		this.checkIdx(startWithIdx);
		this.checkIdx(startWithIdx + amount - 1);
		List<TableCellModel> myCells = getCells();
		LinkedList<TableCellModel> toRemove = new LinkedList<TableCellModel>();
		for (int i = startWithIdx; i < myCells.size(); i++){
			toRemove.add(myCells.get(i));
		}
		myCells.removeAll(toRemove);
		releaseContent();
	}
	
	/**
	 * this only should be used to update columns after the row were changed.
	 * @param idx
	 * @param cellToAdd
	 */
	protected void addCell(int idx, TableCellModel cellToAdd){
		this.checkIdx(idx);
		nullCheck(cellToAdd);
		this.getCells().add(idx, cellToAdd);
		if (this.hasContent() && cellToAdd.getColSpan() == NO_SPAN){
			this.getContent().addAll(cellToAdd.getContent());
		}
	}
	
	protected void updateContent(){
		if (content == null){
			content = new TreeSet<String>();
		}
		for (TableCellModel cell : getCells()){
			if (cell.getColSpan() == NO_SPAN){
				content.addAll(cell.getContent());
			}
		}
	}
	
	//*-----------------------------------------------------------------------*
	//*                           getters/setters                             *
	//*-----------------------------------------------------------------------*
	public int getSizeInRows(){
		List<TableCellModel> cellCollection = getCells();
		if (cellCollection == null){
			return 0;
		} else {
			return cellCollection.size();
		}
	}
	
	public int getNumberOfDistinctCells(){
		List<TableCellModel> cellCollection = getCells();
		if (cellCollection == null || cellCollection.size() == 0){
			return 0;
		}
		TableCellModel previousCell = null;
		int count = 0;
		for (TableCellModel cell : cellCollection){
			if (cell != previousCell){
				count++;
				previousCell = cell;
			}
		}
		return count;
	}
	
	protected void setIndex(int idx){
		index = (idx < 0)? OUTSIDE : idx;
	}

	protected void releaseContent(){
		this.content = null;
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
	//*                            helper methods                             *
	//*-----------------------------------------------------------------------*

	protected void checkIdx(int cellIdx){
		int rowCount = this.getSizeInRows();
		if (cellIdx < 0 || rowCount <= cellIdx){
			throw new IndexOutOfBoundsException(
					"Provided cell index " + cellIdx + " is out of bounds: " +
					"0 - " + (rowCount - 1));
		}
	}

}
/////////////////For future enhancements//////////////////////

/*
public int includesSubColumns(
		List<TableColumnModel> subCols, boolean front, int[]commonRowIdx){
	if (subCols == null || subCols.size() == 0 || this.isEmpty()){
		return 0;
	}
	//empty columns break subColumns sequence.
	//and only nearest columns can be subColumns
	//so our direction depends on "front" parameter
	//example: subCols list is <a, b, c, d, e>
	//and column c is empty
	//if front == true 
	//then the columns d and e still can be subColumns
	//if front == false
	//then the columns a and b still can be subColumns

	//1). check possibility in general
	int start, stop, inc;
	if (front){
		start = subCols.size() - 1;
		stop = 0;
		inc = -1;
	} else {
		start = 0;
		stop = subCols.size() - 1;
		inc = 1;
	}
	boolean possible = true;
	int candidateCount = 0;
	for (int i = start; i != stop && possible; i += inc){
		TableColumnModel col = subCols.get(i);
		if (col.isEmpty()){
			possible = false;
		} else {
			candidateCount++;
		}
	}
	if (candidateCount == 0){
		return candidateCount;
	}
	//2). remove those who are not candidates
	
}
*/
