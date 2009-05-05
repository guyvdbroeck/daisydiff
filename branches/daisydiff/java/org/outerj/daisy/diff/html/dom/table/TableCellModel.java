package org.outerj.daisy.diff.html.dom.table;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import org.outerj.daisy.diff.html.dom.Node;
import org.outerj.daisy.diff.html.dom.TextNode;
import org.outerj.daisy.diff.html.dom.TagNode;
import org.xml.sax.Attributes;

import static org.outerj.daisy.diff.html.dom.table.TableStatics.*;

public class TableCellModel{

	//*-----------------------------------------------------------------------*
	//*                          Cell Model itself                            *
	//*-----------------------------------------------------------------------*
	
	private TagNode cellTagNode = null;
	private boolean headerCell = false;
	private TreeSet<String> content = null;
	private ArrayList<TextNode> textNodes = null;
	private int rowSpan = 0;
	private int colSpan = 0;
	private int rowIndex = OUTSIDE;
	private int colIndex = OUTSIDE;
	
	/**************************************************************************
	 * Creates a model from the cell out of the <code>TagNode</code> tree.
	 * @param cellTag - cell tag in the <code>TagNode</code> tree 
	 * (either "th" or "td")
	 * @throws @see java.lang.IllegalArgumentException - if the 
	 * parameter is <code>null</code> or is not a cell tag
	 */
	public TableCellModel(TagNode cellTag, int rowIdx, int colIdx){
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
			processTextNodes(cellTagNode, textNodes, content);
		}
	}
	
	//*-----------------------------------------------------------------------*
	//*                           getters/setters                             *
	//*-----------------------------------------------------------------------*

	public boolean isHeaderCell(){
		return headerCell;
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
	 * @throws @see java.util.IllegalArgumentException - if the list or 
	 * the set parameter (or both) is <code>null</code>.
	 */
	protected void processTextNodes(
			Node node, List<TextNode> textList, Set<String> contentSet){
		if (textList == null || contentSet == null){
			throw new IllegalArgumentException(
					"Provide collections for output!");
		}
		
		if (node == null){ //no content and TextNodes for null
			return;
		}
		
		if (node instanceof TextNode){//the text node - that's what we need!
			TextNode textNode = (TextNode)node;
			textList.add(textNode);
			String childContent = textNode.getText();
			if (isValuableContent(childContent)){
				contentSet.add(childContent);
			}
			return;
		}
		
		if (node instanceof TagNode){//the tag node - have to dig deeper
			TagNode tag = (TagNode)node;
			if (tag.getNbChildren() > 0){
				for (Node child : tag){
					processTextNodes(child, textList, contentSet);
				}
			}
		}
		return;
	}
}
