package org.outerj.daisy.diff.html.dom.table;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import org.outerj.daisy.diff.html.dom.Node;
import org.outerj.daisy.diff.html.dom.Range;
import org.outerj.daisy.diff.html.dom.TextNode;
import org.outerj.daisy.diff.html.dom.TagNode;
import org.xml.sax.Attributes;

import static org.outerj.daisy.diff.html.dom.table.TableStatics.*;

public class TableCellModel{

	//*-----------------------------------------------------------------------*
	//*                          Cell Model itself                            *
	//*-----------------------------------------------------------------------*
	
	private TagNode cellTagNode = null;
	private Range cellRange = null;
	private boolean headerCell = false;
	private TreeSet<String> content = null;
	private ArrayList<TextNode> textNodes = null;
	private int rowSpan = 0;
	private int colSpan = 0;
	private int rowIndex = OUTSIDE;
	private int colIndex = OUTSIDE;
	private boolean empty = true;
	
	/**************************************************************************
	 * Creates a model from the cell out of the <code>TagNode</code> tree.
	 * @param cellTag - cell tag in the <code>TagNode</code> tree 
	 * (either "th" or "td")
	 * @throws @see java.lang.IllegalArgumentException - if the 
	 * parameter is <code>null</code> or is not a cell tag
	 */
	public TableCellModel(
			TagNode cellTag, int rowIdx, int colIdx, int rangeStart){
		if (cellTag == null){
			throw new IllegalArgumentException(
					"Can't construct the cell model: " +
					"the passed cell tag is null!");
		}
		String name = cellTag.getQName();
		
		//figure whether it's a cell tag and of what kind
		if (HEADER_CELL_TAG_NAME.equals(name)){
			headerCell = true;
		} else if (!CELL_TAG_NAME.equals(name)){
			throw new IllegalArgumentException(
					"Can't construct the cell model: " +
					"the passed tag is not a cell tag!");
		}
		
		//remember the origin
		cellTagNode = cellTag;
		cellRange = new Range(rangeStart);
		
		//remember the position in the table
		rowIndex = (rowIdx < 0)? OUTSIDE : rowIdx;
		colIndex = (colIdx < 0)? OUTSIDE : colIdx;
		
		//is it a spanned cell?
		Attributes attr = cellTagNode.getAttributes();
		String spanString = attr.getValue(ROWSPAN_ATTRIBUTE);
		if (spanString == null){
			rowSpan = NO_SPAN;
		} else {
			try{
				rowSpan = Integer.parseInt(spanString);
			} catch (NumberFormatException ex){
				//debug output:
				System.out.println("couldn't process rowSpan value =" + 
						spanString);
				//end of debug output
				rowSpan = NO_SPAN;
			}
		}
		spanString = attr.getValue(COLSPAN_ATTRIBUTE);
		if (spanString == null){
			colSpan = NO_SPAN;
		} else {
			try{
				colSpan = Integer.parseInt(spanString);
			} catch (NumberFormatException ex){
				//debug output:
				System.out.println("couldn't process colSpan value =" + 
						spanString);
				//end of debug output
				colSpan = NO_SPAN;
			}
		}
		
		if (cellTagNode.getNbChildren() > 0){
			textNodes = new ArrayList<TextNode>();
			content = new TreeSet<String>();
			int count = processTextNodes(cellTagNode, textNodes, content);
			if (cellRange != null){
				cellRange.setEnd(cellRange.getStart() + count - 1);
			}
		}
	}
	
	protected TableCellModel(){
		
	}
	
	public boolean tagParentEquals(TagNode rowTag){
		TagNode cellTagParent = cellTagNode.getParent();
		if (rowTag == cellTagParent){
			return true;
		}
		if (cellTagParent == null){
			return false;
		} else {
			return cellTagParent.equals(rowTag);
		}
	}
	
	public void setTagParent(TagNode rowTag){
		if (rowTag == null || ROW_TAG_NAME.equals(rowTag.getQName())){
			cellTagNode.setParent(rowTag);
		} else {
			throw new IllegalArgumentException(
					"Only ROW tag can be a cell tag parent!");
		}
	}
	
	public TableCellModel copy(){
		TableCellModel cellCopy = new TableCellModel();
		cellCopy.setCellTagNode((TagNode)getCellTagNode().copyTree());
		cellCopy.setRange(cellRange.copy());
		cellCopy.setContent(new TreeSet<String>(content));
		cellCopy.setTextNodes(new ArrayList<TextNode>(textNodes));
		cellCopy.setRowSpan(rowSpan);
		cellCopy.setColSpan(colSpan);
		cellCopy.setEmpty(empty);
		return cellCopy;
	}
	
	public TableCellModel lightCopy(){
		TableCellModel cellCopy = new TableCellModel();
		cellCopy.setCellTagNode((TagNode)getCellTagNode().copyTree());
		cellCopy.setRange(cellRange.copy());
		cellCopy.setTextNodes(new ArrayList<TextNode>(textNodes));
		cellCopy.setRowSpan(rowSpan);
		cellCopy.setColSpan(colSpan);
		cellCopy.setEmpty(empty);
		return cellCopy;
	}
	
	//*-----------------------------------------------------------------------*
	//*                           getters/setters                             *
	//*-----------------------------------------------------------------------*

	public boolean isHeaderCell(){
		return headerCell;
	}
	
	public boolean isEmpty(){
		return empty;
	}
	
	public int getRowSpan(){
		return rowSpan;
	}
	
	public int getColSpan(){
		return colSpan;
	}
	
	public TreeSet<String> getContent(){
		return content;
	}
	
	public int getRowIndex(){
		return rowIndex;
	}
	
	public int getColIndex(){
		return colIndex;
	}
	
	public List<TextNode> getTextNodes(){
		return textNodes;
	}
	
	public int getEndOfRange(){
		if (cellRange == null){
			return Range.NOT_DEFINED;
		} else {
			return cellRange.getEnd();
		}
	}
	
	public TagNode getCellTagNode(){
		return cellTagNode;
	}
	
	protected void setCellTagNode(TagNode cellTag){
		if (cellTag == null){
			throw new IllegalArgumentException(
					"No null parameters allowed!");
		}
		String name = cellTag.getQName();
		
		//figure whether it's a cell tag and of what kind
		if (HEADER_CELL_TAG_NAME.equals(name)){
			headerCell = true;
		} else if (!CELL_TAG_NAME.equals(name)){
			throw new IllegalArgumentException(
					"the passed tag is not a cell tag!");
		}
		
		//remember the origin
		cellTagNode = cellTag;
		
	}
	
	protected void setRange(Range cellRange){
		this.cellRange = cellRange;
	}
	
	protected void setContent(TreeSet<String> newContent){
		this.content = newContent;
	}
	
	protected void setTextNodes(ArrayList<TextNode> textNodes){
		this.textNodes = textNodes;
	}
	
	protected void setRowSpan(int value){
		this.rowSpan = (value < 0)? 0 : value;
	}
	
	protected void setColSpan(int value){
		this.colSpan = (value < 0)? 0 : value;
	}
	
	protected void setRowIndex(int value){
		this.rowIndex = (value < 0)? OUTSIDE : value;
	}
	
	protected void setColIndex(int value){
		this.colIndex = (value < 0)? OUTSIDE : value;
	}
	
	protected void setEmpty(boolean value){
		this.empty = value;
	}
	
	//*-----------------------------------------------------------------------*
	//*                            helper methods                             *
	//*-----------------------------------------------------------------------*

	/**************************************************************************
	 * Recursively adds <code>TextNode</code>s and valuable content to 
	 * the provided collections traversing the given node's subtree.
	 * @param node - the node we want to get the <code>TextNode</code>s and 
	 * the content for
	 * @param textList - where to add the <code>TextNode</code>s-descendants 
	 * @param contentSet - where to add valuable content 
	 * @return amount of processed <code>TextNodes</code>
	 *  (might be different from the amount of elements added to either
	 *  of output collections)
	 * @throws @see java.util.IllegalArgumentException - if the list or 
	 * the set parameter (or both) is <code>null</code>.
	 */
	protected int processTextNodes(
			Node node, List<TextNode> textList, Set<String> contentSet){
		if (textList == null || contentSet == null){
			throw new IllegalArgumentException(
					"Provide collections for output!");
		}
		
		if (node == null){ //no content and TextNodes for null
			return 0;
		}
		
		if (node instanceof TextNode){//the text node - that's what we need!
			TextNode textNode = (TextNode)node;
			String childContent = textNode.getText();
			if (isValuableContent(childContent)){
				contentSet.add(childContent);
			}
			if (isNotWhiteSpace(childContent)){
				textList.add(textNode);
				empty = false;
			}
			return 1;
		}
		
		int count = 0;
		if (node instanceof TagNode){//the tag node - have to dig deeper
			TagNode tag = (TagNode)node;
			if (tag.getNbChildren() > 0){
				for (Node child : tag){
					count += processTextNodes(child, textList, contentSet);
				}
			}
		}
		return count;
	}
}
