package org.outerj.daisy.diff.html.dom.table;

import java.util.List;
import java.util.TreeSet;

import org.outerj.daisy.diff.html.dom.TextNode;
import org.outerj.daisy.diff.html.modification.ModificationType;

public interface ICellSet extends Iterable<TableCellModel>{
	
	public boolean hasContent();
	public boolean isEmpty();
	public List<TableCellModel> getCells();
	public TableCellModel getCell(int idx);
	public TableCellModel getLastCell();
	public List<TextNode> getText();
	public boolean hasCommonContent(ICellSet another);
	public boolean hasSameText(ICellSet another);
	public TreeSet<String> getContent();
	public DistinctCellIterator getDistinctIterator();
	public ModificationType getModification();
	public void setModification(ModificationType mod);
}
