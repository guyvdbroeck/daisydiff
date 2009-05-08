package org.outerj.daisy.diff.html.dom.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.outerj.daisy.diff.html.dom.Node;
import org.outerj.daisy.diff.html.dom.Range;
import org.outerj.daisy.diff.html.dom.TagNode;
import org.outerj.daisy.diff.html.dom.TextNode;
import static org.outerj.daisy.diff.html.dom.table.TableStatics.*;

public class TableModel{
	
	//*-----------------------------------------------------------------------*
	//*                            static methods                             *
	//*-----------------------------------------------------------------------*
	
	/**************************************************************************
	 * This method can check whether the provided node is inside a table 
	 * in the TagNode tree. 
	 * @param toCheck - a node to check
	 * @return <code>null</code> if the node doesn't have a "table" TagNode 
	 * among its ancestors.<br>
	 * the "table" ancestor if it was found.
	 */
	public static TagNode getTableAncestor(Node toCheck){
		//if there's nothing to check - it's not in a table
		if (toCheck == null){
			return null;
		}
		
		//go through all parents or until table tag is found
		do {
			TagNode parent = toCheck.getParent();
			if (parent == null){
				return null;//haven't seen table among ancestors
			}
			if (TABLE_TAG_NAME.equals(parent.getQName())){
				return parent; //it's in the table!
			}
			toCheck = parent;
		} while (true);
	}
	
	
	/**************************************************************************
	 * This method counts amount of <code>TextNode</code>s inside a 
	 * <code>TagNode</code> in the TagNode tree. 
	 * @param toCheck - a node to check
	 * @return amount of descendants <code>TextNode</code>s.
	 */
	public static int getTextNodeCount(Node ancestor){
		if (ancestor == null){
			return 0;
		}
		if (ancestor instanceof TextNode){
			return 1;
		}
		TagNode parent;
		if (ancestor instanceof TagNode){
			parent = (TagNode)ancestor;
		} else {//not a TextNode, not a TagNode - what's that?!
			return 0;
		}
		int textNodeCount = 0;
		for (Node child : parent){
			textNodeCount += getTextNodeCount(child);
		}
		return textNodeCount;
	}
	
	//*-----------------------------------------------------------------------*
	//*                        Table Model itself                             *
	//*-----------------------------------------------------------------------*
	
	private TagNode tableTagNode = null;
	private Range tableRange = null;
	private ArrayList<ICellSet> rows = null;
	private ArrayList<ICellSet> columns = null;
	private TreeSet<String> content = null;
	
	/**************************************************************************
	 * Creates a model from the table out of the <code>TagNode</code> tree.
	 * @param tableTag - table tag in the <code>TagNode</code> tree.
	 * @throws @see java.lang.IllegalArgumentException - if the 
	 * parameter is <code>null</code> or is not a table tag
	 */
	public TableModel(TagNode tableTag, Range tableRange){
		if (tableTag == null || !TABLE_TAG_NAME.equals(tableTag.getQName())) {
			throw new IllegalArgumentException(
					"Can only build a table from not-null TABLE TagNode");
		}
		//remember where we came from
		tableTagNode = tableTag;
		this.tableRange = tableRange;
		//processing the content of the table tag.
		if (tableTagNode.getNbChildren() > 0){
			int rangeIdx = Range.NOT_DEFINED;
			if (tableRange != null){
				rangeIdx = tableRange.getStart();
			}
			rows = new ArrayList<ICellSet>();
			content = new TreeSet<String>();
			int idx = 0;
			//figure out rows
			appendChildRows(tableTagNode, idx, null, rangeIdx);
			//figure out columns
			if (rows.size() > 0){
				//how many columns?
				int colCount = ((TableRowModel)rows.get(0)).getLengthInCols();
				//create them
				columns = new ArrayList<ICellSet>(colCount);
				for (int i = 0; i < colCount; i++){
					columns.add(new TableColumnModel(i));
				}
				//populate them
				for (ICellSet row : rows){
					int i = 0;
					for (TableCellModel cell : row){
						((TableColumnModel)columns.get(i)).appendCell(cell);
						i++;
					}
				}
			}
		}
	}

	public boolean hasCommonContentWith(TableModel another){
		if (another == null){
			throw new IllegalArgumentException(
					"No null arguments allowed");
		}
		if (!another.hasContent() || !this.hasContent()){
			return false;
		}
		//remember - both contents are sorted and do not contain duplicates
		Iterator<String> anotherContent = another.getContent().iterator();
		Iterator<String> myContent = getContent().iterator();
		//there's at least 1 content item or we would returned false
		String otherContentItem = anotherContent.next();
		String myContentItem = myContent.next();
		boolean moreContent = true;
		do{
			int comparisonResult = myContentItem.compareTo(otherContentItem);
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
		return false;
	}
	
	public void addRowToTheTree(TableRowModel deletedRow, int desiredIdx){
		
	}
	
	//*-----------------------------------------------------------------------*
	//*                           getters/setters                             *
	//*-----------------------------------------------------------------------*

	/**************************************************************************
	 * 
	 */
	public TreeSet<String> getContent(){
		return content;
	}
	
	public boolean hasContent(){
		if (content != null && content.size() > 0){
			return true;
		} else {
			return false;
		}
	}
	
	public int getColumnCount(){
		if (this.columns == null){
			return 0;
		} else {
			return columns.size();
		}
	}
	
	public int getRowCount(){
		if (this.rows == null){
			return 0;
		} else {
			return rows.size();
		}
	}
	
	public List<ICellSet> getRows(){
		return rows;
	}
	
	public ICellSet getRow(int idx){
		if (idx < 0 || getRowCount() <= idx){
			return null;
		} else {
			return rows.get(idx);
		}
	}
	
	public TableRowModel getRowCopy(int idx){
		if (idx < 0 || getRowCount() <= idx){
			return null;
		} else {
			return ((TableRowModel)rows.get(idx)).copy();
		}
	}
	
	public List<ICellSet> getColumns(){
		return columns;
	}
	
	//*-----------------------------------------------------------------------*
	//*                            helper methods                             *
	//*-----------------------------------------------------------------------*

	/**************************************************************************
	 * This method assumes that passed parent tag is the table tag or its 
	 * immediate child (like "tbody")
	 */
	protected TableRowModel appendChildRows(
			TagNode parent, int startIndex, 
			TableRowModel previousRow, int rangeStart){
		//make sure we actually have the children tags to process
		if (parent == null || parent.getNbChildren() == 0){
			return previousRow;
		}
		int idx = startIndex;
		//we are interested in the row tags and 
		//will let the rows to handle cell tags.
		//figure out rows:
		for (Node child : parent){
			//is it a row or something else like "thead"?
			if (child != null && child instanceof TagNode){
				TagNode tagChild = (TagNode)child;
				String childName = tagChild.getQName();
				if (ROW_TAG_NAME.equals(childName)){//row child
					//if we had row-spanned cells in the previous row
					//they might belong to this one too 
					if (previousRow != null && 
						previousRow.getMaxSpanDown() >= NO_SPAN){
						previousRow = new TableRowModel(
							tagChild, idx, 
							previousRow.getSpannedDown(), rangeStart);
					} else {
						previousRow = new TableRowModel(
								tagChild, idx, rangeStart);
					}
					rows.add(previousRow);
					content.addAll(previousRow.getContent());
					idx++;
					rangeStart = previousRow.getEndOfRange() + 1;
				} else if (TABLE_SECTION_NAMES.contains(childName)){
					//we are inside "thead", "tbody" or "tfoot" tag
					previousRow = 
						appendChildRows(tagChild, idx, previousRow, rangeStart);
					idx = previousRow.getIndex();
					rangeStart = previousRow.getRange().getEnd() + 1;
				} else {
					//TO DO: process col, colgroup and caption tags
				}
			}
		}
		return previousRow;
	}

}
