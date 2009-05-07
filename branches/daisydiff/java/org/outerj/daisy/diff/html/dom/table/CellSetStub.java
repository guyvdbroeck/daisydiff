package org.outerj.daisy.diff.html.dom.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.outerj.daisy.diff.html.dom.TextNode;

public abstract class CellSetStub implements ICellSet {

	protected ArrayList<TableCellModel> cells;
	protected TreeSet<String> content;
	
	@Override
	public List<TableCellModel> getCells() {
		return cells;
	}

	@Override
	public TreeSet<String> getContent() {
		return content;
	}

	@Override
	public List<TextNode> getText() {
		List<TextNode> text = new ArrayList<TextNode>();
		List<TableCellModel> cellCollection = getCells();
		if (cellCollection == null || cellCollection.size() == 0){
			return text;
		}
		TableCellModel previousCell = null;
		for (TableCellModel cell: cellCollection){
			if (cell != previousCell && !cell.isEmpty()){
				List<TextNode> cellText = cell.getTextNodes();
				text.addAll(cellText);
			}
			previousCell = cell;
		}
		return text;
	}

	@Override
	public boolean hasCommonContent(ICellSet another) {
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
	public boolean hasContent() {
		if (content == null || content.size() == 0){
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean hasSameText(ICellSet another) {
		List<TextNode> thisText = getText();
		List<TextNode> otherText = another.getText();
		return ((thisText.size() == otherText.size()) &&
                 thisText.containsAll(otherText));
	}

	@Override
	public abstract boolean isEmpty();

	@Override
	public abstract Iterator<TableCellModel> iterator();

}
