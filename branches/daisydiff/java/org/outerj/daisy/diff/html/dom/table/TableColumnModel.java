package org.outerj.daisy.diff.html.dom.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

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
	public TableColumnModel(int columnIndex){
		cells = new ArrayList<TableCellModel>();
		content = new TreeSet<String>();
		index = (columnIndex < 0)? OUTSIDE : columnIndex;
	}
	
	public boolean appendCell(TableCellModel cell){
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
			if (cell.getColSpan() == NO_SPAN){
				TreeSet<String> cellContent =cell.getContent();
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
}
