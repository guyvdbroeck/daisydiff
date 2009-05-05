package org.outerj.daisy.diff.html.dom.table;

import java.util.ArrayList;
import java.util.TreeSet;

import static org.outerj.daisy.diff.html.dom.table.TableStatics.*;

public class TableColumnModel{

	//*-----------------------------------------------------------------------*
	//*                        Column Model itself                            *
	//*-----------------------------------------------------------------------*

	private int index = OUTSIDE;
	private ArrayList<TableCellModel> cells = null;
	//content is only from non-spanned cells
	private TreeSet<String> content = null;
	
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
			return true;
		} else {
			return false;
		}
	}

	//*-----------------------------------------------------------------------*
	//*                           getters/setters                             *
	//*-----------------------------------------------------------------------*

}
