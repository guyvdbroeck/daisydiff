package org.outerj.daisy.diff.html.dom.table;

import java.util.List;

import org.eclipse.compare.rangedifferencer.IRangeComparator;

public class CellSetComparator implements IRangeComparator {
	List<CellSet> source;
	
	public CellSetComparator(List<CellSet> source){
		if (source == null){
			throw new IllegalArgumentException(
					"No null parameters allowed");
		}
		this.source = source;
	}
	
	public CellSet getCellSet(int index){
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
		return this.getCellSet(thisIndex).hasCommonContent(
				another.getCellSet(otherIndex));
	}

	@Override
	public boolean skipRangeComparison(int length, int maxLength,
			IRangeComparator other) {
		return false;
	}

}
