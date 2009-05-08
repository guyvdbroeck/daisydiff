package org.outerj.daisy.diff.html.dom.table;

import java.util.Iterator;
import java.util.List;

public class DistinctCellIterator implements Iterator<TableCellModel> {

	private List<TableCellModel> cellList;
	private int startPosition = TableStatics.OUTSIDE;
	private int currentInstancesCount = 0;
	private int nextInstancesCount = 0;
	protected Iterator<TableCellModel> regIterator;
	protected TableCellModel currentCell = null;
	protected TableCellModel nextCell = null;
	
	public DistinctCellIterator(List<TableCellModel> cellList){
		if (cellList == null){
			throw new IllegalArgumentException("No null arguments allowed!");
		}
		this.cellList = cellList;
		regIterator = cellList.iterator();
		startPosition = TableStatics.OUTSIDE;
		getNext();
		if (hasNext()){
			startPosition = 0;
		}
	}
		
	@Override
	public boolean hasNext() {
		return nextCell != null;
	}

	@Override
	public TableCellModel next() {
		if (!hasNext()){
			throw new IndexOutOfBoundsException(
					"No more elements");
		}
		currentCell = nextCell;
		startPosition += currentInstancesCount;
		currentInstancesCount = nextInstancesCount;
		getNext();
		return currentCell;
	}

	public int currentOccurence(){
		return currentInstancesCount;
	}
	
	@Override
	public void remove(){
		for(int i = 0; i < currentInstancesCount; i++){
			TableCellModel candidate = cellList.get(startPosition);
			if (candidate == currentCell){
				cellList.remove(startPosition);
			}
		}
	}
	
	protected void getNext(){
		nextInstancesCount = 0;
		nextCell = currentCell;
		while (regIterator.hasNext() && (nextCell == currentCell)){
			nextInstancesCount++;
			nextCell = regIterator.next();
		}
		if (nextCell == currentCell){
			nextCell = null;
		}
	}

}
