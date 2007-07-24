/*
 * Copyright 2007 Outerthought bvba and Schaubroeck nv
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
package org.outerj.daisy.diff.lcs.rendered;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.rangedifferencer.IRangeComparator;
import org.outerj.daisy.diff.lcs.rendered.dom.BodyNode;
import org.outerj.daisy.diff.lcs.rendered.dom.Node;
import org.outerj.daisy.diff.lcs.rendered.dom.TagNode;
import org.outerj.daisy.diff.lcs.rendered.dom.TextNode;
import org.outerj.daisy.diff.lcs.rendered.dom.helper.LastCommonParentResult;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class LeafComparator extends DefaultHandler implements IRangeComparator {

    private List<TextNode> leafs = new ArrayList<TextNode>(50);

    private BodyNode bodyNode = new BodyNode();

    private TagNode currentParent = bodyNode;

    private boolean documentStarted = false;

    private boolean documentEnded = false;

    public LeafComparator() {
        super();
    }

    public BodyNode getBody() {
        return bodyNode;
    }

    public void startDocument() throws SAXException {
        if (documentStarted)
            throw new IllegalStateException(
                    "This Handler only accepts one document");
        documentStarted = true;
    }

    public void endDocument() throws SAXException {
        if (!documentStarted || documentEnded)
            throw new IllegalStateException();
        endWord();
        documentEnded = true;
        documentStarted = false;
//        getBody().detectIgnorableWhiteSpace();
//        removeIgnorableLeafs();
    }

    private void removeIgnorableLeafs() {
        for(int i=0;i<leafs.size();i++){
            if(leafs.get(i).isIgnorable()){
                leafs.remove(i);
            }
        }
    }

    private boolean bodyStarted = false;

    private boolean bodyEnded = false;

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {

        if (!documentStarted || documentEnded)
            throw new IllegalStateException();

        if (bodyStarted && !bodyEnded) {
            TagNode newTagNode = new TagNode(currentParent, qName, attributes);
            ;
            // +" with parent "+currentParent.getOpeningTag());
            currentParent = newTagNode;
        } else if (bodyStarted) {
            // Ignoring element after body tag closed
        } else if (qName.equalsIgnoreCase("body")) {
            bodyStarted = true;
        }
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {

        if (!documentStarted || documentEnded)
            throw new IllegalStateException();

        if (qName.equalsIgnoreCase("body")) {
            bodyEnded = true;
        } else if (bodyStarted && !bodyEnded) {
            endWord();
            // System.out.println("Ended: " + currentParent.getEndTag());
            currentParent = currentParent.getParent();
        }
    }

    private StringBuilder newWord = new StringBuilder();

    public void characters(char ch[], int start, int length)
            throws SAXException {

        if (!documentStarted || documentEnded)
            throw new IllegalStateException();

        for (int i = start; i < start + length; i++) {
            char c = ch[i];
            if (isValidDelimiter(c)) {
                endWord();
                TextNode textNode = new TextNode(currentParent, Character
                        .toString(c));

                leafs.add(textNode);

            } else {
                newWord.append(c);
            }

        }
    }

    private void endWord() {
        if (newWord.length() > 0) {
            // System.out.println("adding word: " + newWord.toString());
            leafs.add(new TextNode(currentParent, newWord.toString()));
            newWord.setLength(0);
        }
    }

    public int getRangeCount() {
        return leafs.size();
    }

    public TextNode getLeaf(int i) {
        return leafs.get(i);
    }

    public void markAsNew(int begin, int end) {
        for (int i = begin; i < end; i++) {
            getLeaf(i).markAsNew();
        }
    }

    public void compareTags(int i, TextNode node) {
        getLeaf(i).compareTags(node.getParentTree());
    }

    public boolean rangesEqual(int i1, IRangeComparator rangeComp, int i2) {
        LeafComparator comp;
        try {
            comp = (LeafComparator) rangeComp;
        } catch (RuntimeException e) {
            return false;
        }

        return getLeaf(i1).isSameText(comp.getLeaf(i2));
    }

    public boolean skipRangeComparison(int arg0, int arg1, IRangeComparator arg2) {
        return false;
    }

    public static boolean isValidDelimiter(char c) {
        switch (c) {
        // Basic Delimiters
        case '/':
        case '.':
        case '!':
        case ',':
        case ';':
        case '?':
        case ' ':
        case '=':
        case '\'':
        case '"':
        case '\t':
        case '\r':
        case '\n':
            // Extra Delimiters
        case '[':
        case ']':
        case '{':
        case '}':
        case '(':
        case ')':
        case '&':
        case '|':
        case '\\':
        case '-':
        case '_':
        case '+':
        case '*':
        case ':':
            return true;
        default:
            return false;
        }
    }

    public void markAsDeleted(int start, int end, LeafComparator oldComp,
            int before) {

        for (int i = start; i < end; i++) {
            oldComp.getLeaf(i).markAsDeleted(start);
        }
        List<Node> deletedNodes = oldComp.getBody().getMinimalDeletedSet(start);

        //Set prevLeaf to the leaf after which the old HTML needs to be inserted
        Node prevLeaf = null;
        if (before > 0)
            prevLeaf = getLeaf(before - 1);

        //Set nextLeaf to the leaf before which the old HTML needs to be inserted
        Node nextLeaf = null;
        if (before < getRangeCount())
            nextLeaf = getLeaf(before);
        
        System.out.println("Treating new nodes:");
        for (Node node : deletedNodes) {
            System.out.print("node: " + node);
        }

        while (deletedNodes.size() > 0) {
            if (prevLeaf == null && nextLeaf == null) {
                int insertIndex=0;
                TagNode lastCommonParent = getBody();
                prevLeaf = deletedNodes.get(0);
                deletedNodes.remove(0);
                prevLeaf.setParent(lastCommonParent);
                lastCommonParent.addChildBefore(insertIndex, prevLeaf);
            } else if (prevLeaf == null) {
                int insertIndex;
                int index = deletedNodes.size() - 1;
                LastCommonParentResult result=nextLeaf
                .getLastCommonParent(deletedNodes.get(index));
                TagNode lastCommonParent = result.getLastCommonParent();
                insertIndex = result.getIndexInLastCommonParent();

                if (result.isSplittingNeeded()) {
                    System.out.println("splitting needed");
                    nextLeaf.getParent().splitUntill(lastCommonParent,
                            nextLeaf, false);

                    nextLeaf = deletedNodes.get(index);

                    deletedNodes.remove(index);
                    nextLeaf.setParent(lastCommonParent);
                    lastCommonParent.addChildBefore(insertIndex, nextLeaf);
                } else {
                    nextLeaf = deletedNodes.get(index);

                    deletedNodes.remove(index);
                    nextLeaf.setParent(lastCommonParent);
                    lastCommonParent.addChildBefore(insertIndex, nextLeaf);
                }
            } else if (nextLeaf == null) {
                int insertIndex;
                int index = 0;
                LastCommonParentResult result=prevLeaf
                .getLastCommonParent(deletedNodes.get(index));
                TagNode lastCommonParent = result.getLastCommonParent();
                insertIndex = result.getIndexInLastCommonParent() + 1;
                if (result.isSplittingNeeded()) {
                    System.out.println("splitting needed");
                    prevLeaf.getParent().splitUntill(lastCommonParent,
                            prevLeaf, true);

                    prevLeaf = deletedNodes.get(index);
                    deletedNodes.remove(index);
                    prevLeaf.setParent(lastCommonParent);
                    lastCommonParent.addChildBefore(insertIndex, prevLeaf);
                } else {
                    prevLeaf = deletedNodes.get(index);
                    deletedNodes.remove(index);
                    prevLeaf.setParent(lastCommonParent);
                    lastCommonParent.addChildBefore(insertIndex, prevLeaf);
                }
            } else {
                LastCommonParentResult resultPrev=prevLeaf
                .getLastCommonParent(deletedNodes.get(0));;
LastCommonParentResult resultNext=nextLeaf
.getLastCommonParent(deletedNodes.get(deletedNodes
        .size() - 1));;
                TagNode lastCommonParentPrev =resultPrev.getLastCommonParent();
                TagNode lastCommonParentNext = resultNext.getLastCommonParent();
                System.out.println("=========");
                System.out.println("comparing " + deletedNodes.get(0) + " to "
                        + prevLeaf + ":" + resultPrev.getLastCommonParentDepth());

                System.out.println("comparing "
                        + deletedNodes.get(deletedNodes.size() - 1) + " to "
                        + nextLeaf + ":" + resultNext.getLastCommonParentDepth());

                if (resultPrev.getLastCommonParentDepth() >= resultNext
                        .getLastCommonParentDepth()) {
                    int insertIndex = resultPrev.getIndexInLastCommonParent() + 1;
                    int index = 0;
                    if (resultPrev.isSplittingNeeded()) {
                        System.out.println("splitting needed 1");
                        prevLeaf.getParent().splitUntill(lastCommonParentPrev,
                                prevLeaf, true);

                        prevLeaf = deletedNodes.get(index);
                        deletedNodes.remove(index);
                        prevLeaf.setParent(lastCommonParentPrev);
                        lastCommonParentPrev.addChildBefore(insertIndex,
                                prevLeaf);
                    } else {
                        prevLeaf = deletedNodes.get(index);
                        deletedNodes.remove(index);
                        prevLeaf.setParent(lastCommonParentPrev);
                        lastCommonParentPrev.addChildBefore(insertIndex,
                                prevLeaf);
                    }
                } else {
                    int insertIndex = resultNext.getIndexInLastCommonParent();
                    int index = deletedNodes.size() - 1;
                    if (resultNext.isSplittingNeeded()) {
                        System.out.println("splitting needed 2");
                        nextLeaf.getParent().splitUntill(lastCommonParentNext,
                                nextLeaf, false);

                        nextLeaf = deletedNodes.get(index);
                        deletedNodes.remove(index);
                        nextLeaf.setParent(lastCommonParentNext);
                        lastCommonParentNext.addChildBefore(insertIndex,
                                nextLeaf);
                    } else {
                        nextLeaf = deletedNodes.get(index);
                        deletedNodes.remove(index);
                        nextLeaf.setParent(lastCommonParentNext);
                        lastCommonParentNext.addChildBefore(insertIndex,
                                nextLeaf);
                    }

                }

            }
        }
    }

}