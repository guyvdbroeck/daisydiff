package org.outerj.daisy.diff.html.dom.table;

import java.util.List;

import org.eclipse.compare.rangedifferencer.IRangeComparator;

public class CellSetComparator implements IRangeComparator {
	List<ICellSet> source;
	
	public CellSetComparator(List<ICellSet> source){
		if (source == null){
			throw new IllegalArgumentException(
					"No null parameters allowed");
		}
		this.source = source;
	}
	
	public ICellSet getCellSet(int index){
		return source.get(index);
	}
	
	@Override
	public int getRangeCount() {
		if (source == null){
			throw new IllegalStateException(
					"the source isn't initialized");
		}
		return source.size();
	}

	@Override
	public boolean rangesEqual(int thisIndex, IRangeComparator other,
			int otherIndex) {
		CellSetComparator another;
		try{
			another = (CellSetComparator) other;
		} catch (RuntimeException ex){
			return false;
		}
		//get the comparing elements
		ICellSet thisElem = this.getCellSet(thisIndex);
		ICellSet otherElem = another.getCellSet(otherIndex);
		//do they have the content?
		if (thisElem.hasContent()){ 
			if (otherElem.hasContent()){
				return thisElem.hasCommonContent(otherElem);
			} else {
				return false;
			}
		} else {
			if (otherElem.hasContent()){
				return false;
			} else {
				//both sets don't have valuable content - 
				//are they empty?
				if (thisElem.isEmpty()){
					if (otherElem.isEmpty()){
						return true;//both empty
					} else {
						return false;
					}
				} else {
					if (otherElem.isEmpty()){
						return false;
					} else {
						//both are not empty and have no valuable content
						//they might be literally equal
						return thisElem.hasSameText(otherElem);
					}
				}
			}
		}
		
	}

	@Override
	public boolean skipRangeComparison(int length, int maxLength,
			IRangeComparator other) {
		return false;
	}

}
