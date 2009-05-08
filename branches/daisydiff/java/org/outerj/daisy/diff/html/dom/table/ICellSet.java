package org.outerj.daisy.diff.html.dom.table;

import java.util.List;
import java.util.TreeSet;

import org.outerj.daisy.diff.html.dom.TextNode;

public interface ICellSet extends Iterable<TableCellModel>{
	
	public boolean hasContent();
	public boolean isEmpty();
	public List<TableCellModel> getCells();
	public List<TextNode> getText();
	public boolean hasCommonContent(ICellSet another);
	public boolean hasSameText(ICellSet another);
	public TreeSet<String> getContent();
	public DistinctCellIterator getDistinctIterator();
}
