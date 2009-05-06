package org.outerj.daisy.diff.html.dom.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import static org.outerj.daisy.diff.html.dom.table.TableStatics.*;

public class TableColumnModel implements CellSet{

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

	//*-----------------------------------------------------------------------*
	//*                  CellSet interface implementation                     *
	//*-----------------------------------------------------------------------*
	@Override
	public TreeSet<String> getContent(){
		return content;
	}
	
	@Override
	public boolean hasCommonContent(CellSet another){
		try{
			//remember - both contents are sorted and do not contain duplicates
			Iterator<String> anotherContent = another.getContent().iterator();
			Iterator<String> myContent = getContent().iterator();
			String otherContentItem = anotherContent.next();
			String myContentItem = myContent.next();
			boolean moreContent = true;
			do{
				int comparisonResult = 
					myContentItem.compareTo(otherContentItem);
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
		} catch (RuntimeException ex){
			return false;
		}
		return false;
	}
	
	@Override
	public Iterator<TableCellModel> iterator(){
		return cells.iterator();
	}
}
