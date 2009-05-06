package org.outerj.daisy.diff.html.dom.table;

import java.util.TreeSet;

public interface CellSet extends Iterable<TableCellModel>{
	
	public boolean hasCommonContent(CellSet another);
	public TreeSet<String> getContent();
}
