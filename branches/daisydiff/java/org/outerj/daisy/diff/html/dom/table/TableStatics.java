package org.outerj.daisy.diff.html.dom.table;

import static org.outerj.daisy.diff.html.dom.table.TableStatics.TABLE_TAG_NAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import org.outerj.daisy.diff.html.dom.Node;
import org.outerj.daisy.diff.html.dom.TagNode;
import org.outerj.daisy.diff.html.dom.TextNode;

public class TableStatics {

	//*-----------------------------------------------------------------------*
	//*                          static constants                             *
	//*-----------------------------------------------------------------------*
	
	public static final String TABLE_TAG_NAME = "table";
	public static final String ROW_TAG_NAME = "tr";
	public static final String HEADER_CELL_TAG_NAME = "th";
	public static final String CELL_TAG_NAME = "td";
	public static final String ROWSPAN_ATTRIBUTE = "rowspan";
	public static final String COLSPAN_ATTRIBUTE = "colspan";
	public static final int NO_SPAN = 1;
	public static final int OUTSIDE = -1;
	public static final String NBSP = "\240";
	public static final String SPACE = " ";

	public static final SortedSet<String> TOO_COMMON;
	static{
		// TODO make that piece international external config
		TOO_COMMON = Collections.unmodifiableSortedSet(
				new TreeSet<String>(
				Arrays.asList(new String[] 
						{
						  "a", "an", "the", "and", "but",
						  "of", "for", "from", "by", "at",
						  "$", ",", ".", ":", ";", "!", "?", 
						  "-", "(", ")",
						  NBSP
						})));
	}
	
	public static final ArrayList<String> TABLE_SECTION_NAMES;
	static {
		TABLE_SECTION_NAMES = new ArrayList<String>(
				Arrays.asList(new String[]{"thead", "tbody", "tfoot"}));
		
	}
	
	//*-----------------------------------------------------------------------*
	//*                            static methods                             *
	//*-----------------------------------------------------------------------*
	
	/**************************************************************************
	 * Checks if the provided text is a valuable content
	 * @param text - text to check
	 * @return <code>false</code> if the provided text <br>
	 * is found in the list of "too common" words,<br>
	 * or is <code>null</code><br>
	 * or is an empty string.<br>
	 */
	public static boolean isValuableContent(String text){
		if (text == null || text.length() == 0){
			return false;
		}
		if (TOO_COMMON.contains(text)){
			return false;
		} else {
			return true;
		}
	}
	
	public static boolean isNotWhiteSpace(String text){
		if (text == null || text.length() == 0){
			throw new IllegalArgumentException(
					"No null or empty parameters allowed"); 
		}
		text = text.replaceAll("\\s|" + NBSP, SPACE);
		text = text.trim();
		if (text.length() == 0){
			return false;
		} else {
			return true;
		}
	}

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
	
	public static TagNode getEmptyCell(){
		TagNode cellTag = new TagNode(CELL_TAG_NAME);
		TextNode nbsp = new TextNode(cellTag, NBSP);
		cellTag.addChild(nbsp);
		return cellTag;
	}
}
