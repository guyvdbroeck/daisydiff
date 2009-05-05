/*
 * Copyright 2007 Guy Van den Broeck
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.outerj.daisy.diff.html;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.eclipse.compare.rangedifferencer.IRangeComparator;
import org.outerj.daisy.diff.html.ancestor.AncestorComparator;
import org.outerj.daisy.diff.html.ancestor.AncestorComparatorResult;
import org.outerj.daisy.diff.html.dom.BodyNode;
import org.outerj.daisy.diff.html.dom.DomTree;
import org.outerj.daisy.diff.html.dom.Node;
import org.outerj.daisy.diff.html.dom.Range;
import org.outerj.daisy.diff.html.dom.TextNode;
import org.outerj.daisy.diff.html.dom.TagNode;
import org.outerj.daisy.diff.html.dom.helper.LastCommonParentResult;
import org.outerj.daisy.diff.html.modification.Modification;
import org.outerj.daisy.diff.html.modification.ModificationType;
import org.xml.sax.helpers.AttributesImpl;

import static org.outerj.daisy.diff.html.dom.table.TableStatics.*;
/**
 * A comparator that generates a DOM tree of sorts from handling SAX events.
 * Then it can be used to compute the difference between DOM trees and mark
 * elements accordingly.
 */
public class TextNodeComparator implements IRangeComparator, Iterable<TextNode> {

    private List<TextNode> textNodes = new ArrayList<TextNode>(50);
    //table range index for easy check whether the text node is
    //within a table, whether they are in the same table etc.
    private HashMap<TagNode, Range> tablesBoundaries;

    private List<Modification> lastModified = new ArrayList<Modification>();

    private BodyNode bodyNode;

    private Locale locale;

    public TextNodeComparator(DomTree tree, Locale locale) {
        super();
        this.locale = locale;
        textNodes = tree.getTextNodes();
        bodyNode = tree.getBodyNode();
        tablesBoundaries = tree.getTablesBoundaries();
    }

    public BodyNode getBodyNode() {
        return bodyNode;
    }

    public int getRangeCount() {
        return textNodes.size();
    }

    public TextNode getTextNode(int i) {
        return textNodes.get(i);
    }

    /**
     * @return <code>true</code> if any of the 
     * <code>TextNode</code>s from the collection of this
     * <code>TextNodeComparator</code> belong to the table.
     * The check is very fast, because the table ranges
     * are constructed before the <code>TextNodeComparator</code>
     * construction and the check only for the ranges list
     * to have any elements.
     */
    public boolean hasTableContent(){
    	return (tablesBoundaries != null &&
    			tablesBoundaries.size() > 0);
    }
    
    /**
     * @return the whole list of ranges for all the table.
     * Range for the table is a pair of indices (of the first and last
     * <code>TextNode</code> of the table) in the collection
     * of the <code>TextNode</code>s of this
     * <code>TextNodeComparator</code>.
     * The returned list is sorted by the first index and the nested
     * table ranges are following their parent.
     */
    public List<Range> getTablesRanges(){
    	if (tablesBoundaries == null){
    		return null;
    	} else {
    		ArrayList<Range> tablesRanges = 
    			new ArrayList<Range>(tablesBoundaries.values());
    		Collections.sort(tablesRanges);
    		return tablesRanges;
    	}
    }
    
    /**
     * fetches the range of the provided table.
     * The range is the pair of indices - the index
     * of the first <code>TextNode</code> that belongs to the table
     * and the index of the last <code>TextNode</code> of the table.
     * Indices of the elements in the collection of <code>TextNode</code>s
     * of this <code>TextNodeComparator</code>.
     * @param tableTag - the table to fetch the range for
     * @return <code>Range</code> of the provided table.
     * @throws <code>java.land.IllegalArgumentException</code> 
     * if the parameter is <code>null</code>.
     */
    public Range getTableRange(TagNode tableTag){
    	if (tableTag == null){
    		throw new IllegalArgumentException(
    				"No null parameters are allowed!");
    	}
    	return tablesBoundaries.get(tableTag);
    }
    
    private long newID = 0;

    public void markAsNew(int start, int end) {
        if (end <= start)
            return;

        if (whiteAfterLastChangedPart)
            getTextNode(start).setWhiteBefore(false);

        List<Modification> nextLastModified = new ArrayList<Modification>();

        for (int i = start; i < end; i++) {
            Modification mod = new Modification(ModificationType.ADDED);
            mod.setID(newID);
            if (lastModified.size() > 0) {
                mod.setPrevious(lastModified.get(0));
                if (lastModified.get(0).getNext() == null) {
                    for (Modification lastMod : lastModified) {
                        lastMod.setNext(mod);
                    }
                }
            }
            nextLastModified.add(mod);
            getTextNode(i).setModification(mod);
        }
        getTextNode(start).getModification().setFirstOfID(true);
        newID++;
        lastModified = nextLastModified;
    }

    public boolean rangesEqual(int i1, IRangeComparator rangeComp, int i2) {
        TextNodeComparator comp;
        try {
            comp = (TextNodeComparator) rangeComp;
        } catch (RuntimeException e) {
            return false;
        }

        return getTextNode(i1).isSameText(comp.getTextNode(i2));
    }

    public boolean skipRangeComparison(int arg0, int arg1, IRangeComparator arg2) {
        return false;
    }

    private long changedID = 0;

    private boolean changedIDUsed = false;

    public void handlePossibleChangedPart(int leftstart, int leftend,
            int rightstart, int rightend, TextNodeComparator leftComparator) {
        int i = rightstart;
        int j = leftstart;

        if (changedIDUsed) {
            changedID++;
            changedIDUsed = false;
        }

        List<Modification> nextLastModified = new ArrayList<Modification>();

        String changes = null;
        while (i < rightend) {
            AncestorComparator acthis = new AncestorComparator(getTextNode(i)
                    .getParentTree());
            AncestorComparator acother = new AncestorComparator(leftComparator
                    .getTextNode(j).getParentTree());

            AncestorComparatorResult result = acthis.getResult(acother, locale);

            if (result.isChanged()) {

                Modification mod = new Modification(ModificationType.CHANGED);

                if (!changedIDUsed) {
                    mod.setFirstOfID(true);
                    if (nextLastModified.size() > 0) {
                        lastModified = nextLastModified;
                        nextLastModified = new ArrayList<Modification>();
                    }
                } else if (result.getChanges() != null
                        && !result.getChanges().equals(changes)) {
                    changedID++;
                    mod.setFirstOfID(true);
                    if (nextLastModified.size() > 0) {
                        lastModified = nextLastModified;
                        nextLastModified = new ArrayList<Modification>();
                    }
                }

                if (lastModified.size() > 0) {
                    mod.setPrevious(lastModified.get(0));
                    if (lastModified.get(0).getNext() == null) {
                        for (Modification lastMod : lastModified) {
                            lastMod.setNext(mod);
                        }
                    }
                }
                nextLastModified.add(mod);

                mod.setChanges(result.getChanges());
                mod.setID(changedID);

                getTextNode(i).setModification(mod);
                changes = result.getChanges();
                changedIDUsed = true;
            } else if (changedIDUsed) {
                changedID++;
                changedIDUsed = false;
            }

            i++;
            j++;
        }

        if (nextLastModified.size() > 0)
            lastModified = nextLastModified;

    }

    // used to remove the whitespace between a red and green block
    private boolean whiteAfterLastChangedPart = false;

    private long deletedID = 0;

    public void markAsDeleted(int start, int end, TextNodeComparator oldComp,
            int before) {

        if (end <= start)
            return;

        if (before > 0 && getTextNode(before - 1).isWhiteAfter()) {
            whiteAfterLastChangedPart = true;
        } else {
            whiteAfterLastChangedPart = false;
        }

        List<Modification> nextLastModified = new ArrayList<Modification>();

        for (int i = start; i < end; i++) {
            Modification mod = new Modification(ModificationType.REMOVED);
            mod.setID(deletedID);
            if (lastModified.size() > 0) {
                mod.setPrevious(lastModified.get(0));
                if (lastModified.get(0).getNext() == null) {
                    for (Modification lastMod : lastModified) {
                        lastMod.setNext(mod);
                    }
                }
            }
            nextLastModified.add(mod);

            // oldComp is used here because we're going to move its deleted
            // elements
            // to this tree!
            oldComp.getTextNode(i).setModification(mod);
        }
        oldComp.getTextNode(start).getModification().setFirstOfID(true);

        List<Node> deletedNodes = oldComp.getBodyNode().getMinimalDeletedSet(
                deletedID);

        // Set prevLeaf to the leaf after which the old HTML needs to be
        // inserted
        Node prevLeaf = null;
        if (before > 0)
            prevLeaf = getTextNode(before - 1);

        // Set nextLeaf to the leaf before which the old HTML needs to be
        // inserted
        Node nextLeaf = null;
        if (before < getRangeCount())
            nextLeaf = getTextNode(before);


        while (deletedNodes.size() > 0) {
            LastCommonParentResult prevResult, nextResult;
            if (prevLeaf != null) {
                prevResult = prevLeaf.getLastCommonParent(deletedNodes
                        .get(0));
            	//a) do adjustment for parents that 
            	//should not be inserted in
                adjustCommonParent(prevResult);
            } else {
                prevResult = new LastCommonParentResult();
                prevResult.setLastCommonParent(getBodyNode());
                prevResult.setIndexInLastCommonParent(-1);
            }
            if (nextLeaf != null) {
                nextResult = nextLeaf.getLastCommonParent(deletedNodes
                        .get(deletedNodes.size() - 1));
            	//a) do adjustment for parents that 
            	//should not be inserted in
                adjustCommonParent(nextResult);
            } else {
                nextResult = new LastCommonParentResult();
                nextResult.setLastCommonParent(getBodyNode());
                nextResult.setIndexInLastCommonParent(getBodyNode()
                        .getNbChildren());
            }

            if (prevResult.getLastCommonParentDepth() == nextResult
                    .getLastCommonParentDepth()) {
                // We need some metric to choose which way to add...
                if (deletedNodes.get(0).getParent() == deletedNodes.get(
                        deletedNodes.size() - 1).getParent()
                        && prevResult.getLastCommonParent() == nextResult
                        .getLastCommonParent()) {
                    // The difference is not in the parent
                    prevResult.setLastCommonParentDepth(prevResult
                            .getLastCommonParentDepth() + 1);

                } else {
                    // The difference is in the parent, so compare them
                    // now THIS is tricky
                    double distancePrev = deletedNodes
                    .get(0)
                    .getParent()
                    .getMatchRatio(prevResult.getLastCommonParent());
                    double distanceNext = deletedNodes
                    .get(deletedNodes.size() - 1)
                    .getParent()
                    .getMatchRatio(nextResult.getLastCommonParent());

                    if (distancePrev <= distanceNext) {
                        prevResult.setLastCommonParentDepth(prevResult
                                .getLastCommonParentDepth() + 1);
                    } else {
                        nextResult.setLastCommonParentDepth(nextResult
                                .getLastCommonParentDepth() + 1);
                    }
                }

            }

            if (prevResult.getLastCommonParentDepth() > nextResult
                    .getLastCommonParentDepth()) {

                // Inserting at the front
                if (prevResult.isSplittingNeeded()) {
                	//b) do a safe splitting
                	safeSplit(prevResult,prevLeaf,true);
                	//c) could probably create an annotation 
                	//from which context the node was removed.
                }
                //we will need an adjustment for deleted nodes that
                //are not supposed to be on their own (like <td> or <li>)
                prevLeaf = adjustDeletedNodes(deletedNodes, true);
                prevLeaf.setParent(prevResult.getLastCommonParent());
                prevResult.getLastCommonParent().addChild(
                        prevResult.getIndexInLastCommonParent() + 1,
                        prevLeaf);

            } else if (prevResult.getLastCommonParentDepth() < nextResult
                    .getLastCommonParentDepth()) {
                // Inserting at the back
                if (nextResult.isSplittingNeeded()) {
                    boolean splitOccured = 
                    	//b) do a safe splitting
                    	safeSplit(nextResult, nextLeaf, false);
                    if (splitOccured) {
                        // The place where to insert is shifted one place to the
                        // right
                        nextResult.setIndexInLastCommonParent(nextResult
                                .getIndexInLastCommonParent() + 1);
                    }
                }
            	//c) could probably create an annotation 
            	//from which context the node was removed.

                //we will need an adjustment for deleted nodes that
                //are not supposed to be on their own (like <td> or <li>)
                nextLeaf = adjustDeletedNodes(deletedNodes, false);
                nextLeaf.setParent(nextResult.getLastCommonParent());
                nextResult.getLastCommonParent().addChild(
                        nextResult.getIndexInLastCommonParent(), nextLeaf);
            } else
                throw new IllegalStateException();

        }
        lastModified = nextLastModified;
        deletedID++;
    }

    public void expandWhiteSpace() {
        getBodyNode().expandWhiteSpace();
    }

    public Iterator<TextNode> iterator() {
        return textNodes.iterator();
    }
    
    /**************************************************************************
     * This method prevents insertion of VARIOUS HTML elements
     * in the structures that can have only certain type of children.
     * Example of the structures are: table, ul, ol, dl, tr, tbody etc.
     * To prevent this insertion the common parent is adjusted to the 
     * closest outer ancestor that does not have that limitation
     * (so if the list is the last common parent, then the list's parent
     * will be the common parent. If the table row is the common parent,
     * then the table's parent will be the common parent (not the row's parent)
     * @param comParentResult - not yet adjusted common parent 
     * (will be modified).
     */
    protected void adjustCommonParent(LastCommonParentResult comParentResult){
        TagNode commonParent = comParentResult.getLastCommonParent();
        Node child = commonParent.getChild(
        		comParentResult.getIndexInLastCommonParent());
        int depthDecrease = 0;
        while (commonParent.isSpecial()){
        	child = commonParent;
        	commonParent = commonParent.getParent();
        	depthDecrease++;
        }
        if (depthDecrease > 0){
        	comParentResult.setLastCommonParent(commonParent);
        	comParentResult.setIndexInLastCommonParent(
        		commonParent.getIndexOf(child));
        	comParentResult.setLastCommonParentDepth(
        		comParentResult.getLastCommonParentDepth() - depthDecrease);
        }
    	
    }
    
    /**************************************************************************
     * This method prevents splitting of the HTML structures that allow only
     * certain kind of children (like tables or lists). 
     * Splitting of these structures would often result in invalid HTML and
     * distorted document.
     * @param comParentResult - last common parent result
     * @param delimiter - the element to split on
     * @param includeLeft - <code>true</code> if the delimiter should
     * belong to the "left" (first) part after the splitting
     * @return true if any HTML elements were actually split.
     */
    protected boolean safeSplit(
    		LastCommonParentResult comParentResult,
    		Node delimiter,
    		boolean includeLeft){
    	TagNode bottomPatient = delimiter.getParent();
    	TagNode noSplitAncestor = comParentResult.getLastCommonParent();
    	if (bottomPatient == null || noSplitAncestor == null){
    		return false;
    	}
    	//a) determine real bottomPatient 
    	//to avoid splitting of non-split structures
    	//a1): get all the ancestors
    	List<TagNode> parentTree = delimiter.getParentTree();
    	//a2): skip all ancestors from root to noSplitAncestor
    	Iterator<TagNode> ancestors = parentTree.iterator();
    	//the iterator has at least one - the "bottomPatient"
    	TagNode ancestor = ancestors.next(); 
    	while (ancestor != noSplitAncestor && ancestors.hasNext()){
    		ancestor = ancestors.next();
    	}
    	if (ancestor != noSplitAncestor){
    		//means we didn't found the split stopper among the ancestors
    		return false;
    	}
    	//a3): descent toward the bottomPatient, but do not go past
    	//noSplit structures
    	while(ancestor != bottomPatient && 
    		  !ancestor.isSpecial() &&
    		  ancestors.hasNext()){
    		ancestor = ancestors.next();
    	}
    	if (ancestor.isSpecial()){
    		//we have found special structure that 
    		//we don't want to split - 
    		//then we need previous ancestor
    		bottomPatient = ancestor.getParent();
    		//and different delimiter (immediate child)
    		delimiter = ancestor;
    		//also, we want to put the diff AFTER that structure?
    		//includeLeft = true;
    	}
    	//b) call the regular split.
    	boolean result = bottomPatient.splitUntill(
    			noSplitAncestor, delimiter, includeLeft);
    	return result;
    }
    
    /**************************************************************************
     * The deleted nodes have to be inserted in the new tree.
     * However, certain kind of nodes should have only certain kind of parents
     * (e.g. you only can place td in tr, and not in p or span). This method
     * "undresses" the content of such "special" nodes and 
     * puts its copy in a div element.
     * The regular nodes are not changed(just copied). 
     * The returning node is deleted from the list (the parameter).
     * @param list - current list of the deleted nodes
     * @param beginning - <code>true</code> if we are considering first element
     * @return the processed node (either original one or the div).
     */
    protected Node adjustDeletedNodes(
    		List<Node> list, boolean beginning){
    	if (list == null || list.size() == 0){
    		return null;
    	}
    	
		int startIdx, increment;
		if (beginning){
			//moving from the beginning of the list toward the end
			startIdx = 0;
			increment = 1;
		} else {
			//moving from the end of the list toward the beginning
			startIdx = list.size() - 1;
			increment = -1;
		}
		Node deleted = list.get(startIdx);
		
		if (!(deleted instanceof TagNode)){
    		return list.remove(startIdx).copyTree();
    	}
    	//it's a TagNode
    	TagNode deletedTag = (TagNode)deleted;
    	String delName = deletedTag.getQName();
    	//tbody, thead, tfoot
    	if (TABLE_SECTION_NAMES.contains(delName)){
    		deletedTag = (TagNode)list.remove(startIdx).copyTree();
    		TagNode tableTag = deletedTag.shallowCopy(TABLE_TAG_NAME);
    		Iterator<Node> children = deletedTag.iterator();
    		while (children.hasNext()){
    			Node child = children.next();
    			child.setParent(tableTag);
    			tableTag.addChild(child);
    		}
            tableTag.setWhiteBefore(deletedTag.isWhiteBefore());
            tableTag.setWhiteAfter(deletedTag.isWhiteAfter());
            return tableTag;
    	}
    	//others
    	if (deletedTag.needsSpecialParent()){
    		//it's a special tag  that should be inside certain one
    		//like <tr> or <dt> or <li>
    		//combine it with other similar children
    		List<Node> similarOrphans = new ArrayList<Node>();
    		similarOrphans.add(deletedTag);
			TagNode oldParent = deletedTag.getParent();
			int idx = startIdx + increment;
			int limit = (beginning)? list.size() : -1;
			boolean keepGoing = true;
			
    		if (list.size() > 1){
    			do {
    				Node otherKid = list.get(idx);
    				idx += increment;
   					keepGoing =	(oldParent == otherKid.getParent());
   					if (keepGoing){
    					similarOrphans.add(otherKid);
    				}
    				keepGoing = keepGoing && 
    					((beginning)? (idx < limit):(idx > limit));
    			} while (keepGoing);
    		}
			//now we have gathered all similar children 
			//- put them together in a paragraph
			//a) create a div because it can contain both:
    		//blocks and in-line elements
			String name = "div";
			TagNode newParent = new TagNode(null, name, new AttributesImpl());
			
			if (!beginning){
 				Collections.reverse(similarOrphans);
			}
			adoptAll(similarOrphans, newParent);
			list.removeAll(similarOrphans);
			return newParent;
    	} else {
    		return list.remove(startIdx).copyTree();
    	}
    }
    
    /**************************************************************************
     * for each kid either its copy or the copy of its content 
     * will be put in the newParent. The copy of the content instead of copy
     * of the kid is done when the kid require special kind of parents 
     * (e.g. td(table cell) allows only tr(table row) as its parent).
     * This method is recursive.
     * @param kids - the list of kids to copy
     * @param newParent - the parent for the copies
     */
    protected void adoptAll(List<Node> kids, TagNode newParent){
    	if (kids == null || kids.size() == 0 || newParent == null){
    		return;
    	}
    	for (Node child : kids){
    		if (child instanceof TagNode){
    			TagNode tagChild = (TagNode)child;
    			if (tagChild.needsSpecialParent()){
					//then add its children, not itself
					Iterator<Node> tableDescendants = 
						tagChild.iterator();
					ArrayList<Node> moreKids = new ArrayList<Node>();
					while (tableDescendants.hasNext()){
						moreKids.add(tableDescendants.next());
					}
					adoptAll(moreKids, newParent);
					//add a new line after if it's not a cell
					String childName = tagChild.getQName();
					//add a new line after other than cell specials
					if (!CELL_TAG_NAME.equals(childName) &&
						!HEADER_CELL_TAG_NAME.equals(childName)){
						newParent.addChild(new TagNode(
								newParent, "br", new AttributesImpl()));
					}
					continue;
    			}
    		}
   			Node aChild = child.copyTree();
   			aChild.setParent(newParent);
   			newParent.addChild(aChild);
    	}
    }
}