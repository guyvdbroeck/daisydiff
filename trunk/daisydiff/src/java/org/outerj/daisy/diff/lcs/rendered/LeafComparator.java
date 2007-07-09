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
                TextNode textNode = new TextNode(currentParent, Character.toString(c));

                leafs.add(textNode);
                currentParent.addChild(textNode);
                

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
            int before, boolean reconstructTags) {

        if (!reconstructTags) {
            int cut = start;
            boolean cutFound = false;

            TextNode prevLeaf = null;
            if (before > 0)
                prevLeaf = getLeaf(before - 1);
            else {
                cut = start;
                cutFound = true;
                System.out.println("cut at start");
            }

            TextNode nextLeaf = null;
            if (before < getRangeCount())
                nextLeaf = getLeaf(before);
            else {
                cut = end + 1;
                cutFound = true;

                System.out.println("cut at end");
            }

            int[] prevD = new int[end - start];
            int[] nextD = new int[end - start];

            if (!cutFound) {
                for (int i = start; i < end; i++) {
                    prevD[i - start] = prevLeaf.getTagDistance(oldComp.getLeaf(
                            i).getParentTree());
                    nextD[i - start] = nextLeaf.getTagDistance(oldComp.getLeaf(
                            i).getParentTree());
                    System.out.println(i - start + ": " + prevD[i - start]
                            + " vs " + nextD[i - start]);
                }

                long min = Long.MAX_VALUE;
                cut = start;
                for (int i = start; i < end + 1; i++) {
                    long sum = 0;
                    for (int j = start; j < i; j++) {
                        sum += prevD[j - start];
                    }
                    for (int j = i; j < end; j++) {
                        sum += nextD[j - start];
                    }
                    System.out.println("sum for " + i + " is " + sum);
                    if (sum < min) {
                        min = sum;
                        cut = i;
                    }
                }
            }

            System.out.println("cut found at " + cut + " between " + start
                    + " and " + end);
            // System.out.println(prevLeaf.getText()+" -> "+nextLeaf.getText());

            TagNode parent = prevLeaf.getParent();
            int insertPoint = parent.getIndexOf(prevLeaf) + 1;
            for (int i = start; i < cut; i++) {
                TextNode t = oldComp.getLeaf(i);

                t.setDeleted();
                t.setParent(parent);
                parent.addChildBefore(insertPoint++, t);
            }
            parent = nextLeaf.getParent();
            for (int i = cut; i < end; i++) {
                TextNode t = oldComp.getLeaf(i);
                t.setDeleted();
                t.setParent(parent);
                parent.addChildBefore(parent.getIndexOf(nextLeaf), t);
            }
        } else {

            for (int i = start; i < end; i++) {
                oldComp.getLeaf(i).markAsDeleted(start);
                oldComp.getLeaf(i).setDeleted();
            }
            List<Node> deletedNodes = oldComp.getBody().getMinimalDeletedSet(
                    start);

            Node prevLeaf = null;
            if (before > 0)
                prevLeaf = getLeaf(before - 1);
            else {
                prevLeaf = null;
            }
            Node nextLeaf = null;
            if (before < getRangeCount())
                nextLeaf = getLeaf(before);
            else {
                nextLeaf = null;
            }

            while (deletedNodes.size() > 0) {

                if (prevLeaf == null && nextLeaf == null) {
                    int insertIndex;
                    TagNode lastCommonParent = getBody();
                    insertIndex = 0;
                    prevLeaf = deletedNodes.get(0);
                    deletedNodes.remove(0);
                    prevLeaf.setParent(lastCommonParent);
                    lastCommonParent.addChildBefore(insertIndex, prevLeaf);
                } else if (prevLeaf == null) {
                    int insertIndex;
                    int index = deletedNodes.size() - 1;
                    TagNode lastCommonParent = nextLeaf
                            .getLastCommonParent(deletedNodes.get(index));
                    insertIndex = nextLeaf.getLastCommonParentIndex();
                    nextLeaf = deletedNodes.get(index);
                    deletedNodes.remove(index);
                    nextLeaf.setParent(lastCommonParent);
                    lastCommonParent.addChildBefore(insertIndex, nextLeaf);
                } else if (nextLeaf == null) {
                    int insertIndex;
                    int index = 0;
                    TagNode lastCommonParent = prevLeaf
                            .getLastCommonParent(deletedNodes.get(index));
                    insertIndex = prevLeaf.getLastCommonParentIndex() + 1;
                    prevLeaf = deletedNodes.get(index);
                    deletedNodes.remove(index);
                    prevLeaf.setParent(lastCommonParent);
                    lastCommonParent.addChildBefore(insertIndex, prevLeaf);
                } else {

                    TagNode lastCommonParentPrev = prevLeaf
                            .getLastCommonParent(deletedNodes.get(0));
                    TagNode lastCommonParentNext = nextLeaf
                            .getLastCommonParent(deletedNodes.get(deletedNodes.size() - 1));

                    if (prevLeaf.getLastCommonParentDepth() >= nextLeaf
                            .getLastCommonParentDepth()) {
                        int insertIndex = prevLeaf.getLastCommonParentIndex() + 1;
                        int index = 0;
                        prevLeaf = deletedNodes.get(index);
                        deletedNodes.remove(index);
                        prevLeaf.setParent(lastCommonParentPrev);
                        lastCommonParentPrev.addChildBefore(insertIndex,
                                prevLeaf);
                    } else {
                        int insertIndex = nextLeaf.getLastCommonParentIndex();
                        int index = deletedNodes.size() - 1;
                        nextLeaf = deletedNodes.get(index);
                        deletedNodes.remove(index);
                        nextLeaf.setParent(lastCommonParentNext);
                        lastCommonParentNext.addChildBefore(insertIndex,
                                nextLeaf);
                    }

                }

            }

            // for (int i = 0; i < deletedNodes.size(); i++) {
            //                
            //                
            //                
            // TagNode lastcommonparent;
            // int afterLastcommonparentIndex;
            //                
            // if (prevLeaf == null) {
            // lastcommonparent = getBody();
            // afterLastcommonparentIndex = 0;
            // System.out.println("Setting afterLastcommonparent to 0.");
            // } else {
            // lastcommonparent = prevLeaf
            // .getLastCommonParent(deletedNodes.get(i));
            // afterLastcommonparentIndex = prevLeaf
            // .getAfterLastCommonParentIndex();
            // System.out.println("Afterlast = "+afterLastcommonparentIndex);
            // }
            // int temp = lastcommonparent.getNbChildren();
            // deletedNodes.get(i).setParent(lastcommonparent);
            //                lastcommonparent.addChildBefore(afterLastcommonparentIndex,
            //                        deletedNodes.get(i));
            //                prevLeaf = deletedNodes.get(i);
            //
            //            }

        }
    }

}