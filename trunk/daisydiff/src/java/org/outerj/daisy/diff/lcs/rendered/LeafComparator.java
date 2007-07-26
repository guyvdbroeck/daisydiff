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
import org.outerj.daisy.diff.lcs.rendered.dom.ImageNode;
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

    @Override
    public void startDocument() throws SAXException {
        if (documentStarted)
            throw new IllegalStateException(
                    "This Handler only accepts one document");
        documentStarted = true;
    }

    @Override
    public void endDocument() throws SAXException {
        if (!documentStarted || documentEnded)
            throw new IllegalStateException();
        endWord();
        documentEnded = true;
        documentStarted = false;
        getBody().detectIgnorableWhiteSpace();
        getBody().removeIgnorableWhiteSpace();
        removeIgnorableLeafs();
    }

    private void removeIgnorableLeafs() {
        List<TextNode> toRemove = new ArrayList<TextNode>(30);
        for (TextNode leaf : leafs) {
            if (leaf.isIgnorable()) {
                toRemove.add(leaf);
            }
        }
        leafs.removeAll(toRemove);
    }

    private boolean bodyStarted = false;

    private boolean bodyEnded = false;

    @Override
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

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {

        if (!documentStarted || documentEnded)
            throw new IllegalStateException();

        if (qName.equalsIgnoreCase("body")) {
            bodyEnded = true;
        } else if (bodyStarted && !bodyEnded) {
            if (qName.equalsIgnoreCase("img")) {
                // Insert a dummy leaf for the image
                leafs.add(new ImageNode(currentParent, currentParent
                        .getAttributes()));
            }
            endWord();
            currentParent = currentParent.getParent();
        }
    }

    private StringBuilder newWord = new StringBuilder();

    @Override
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

        // Set prevLeaf to the leaf after which the old HTML needs to be
        // inserted
        Node prevLeaf = null;
        if (before > 0)
            prevLeaf = getLeaf(before - 1);

        // Set nextLeaf to the leaf before which the old HTML needs to be
        // inserted
        Node nextLeaf = null;
        if (before < getRangeCount())
            nextLeaf = getLeaf(before);

        // System.out.println("Treating new nodes:");
        // System.out.println("===================");
        // for (Node node : deletedNodes) {
        // System.out.print(node);
        // }
        // System.out.println("\n===================");

        while (deletedNodes.size() > 0) {
            LastCommonParentResult prevResult, nextResult;
            if (prevLeaf != null) {
                prevResult = prevLeaf.getLastCommonParent(deletedNodes.get(0));
            } else {
                prevResult = new LastCommonParentResult();
                prevResult.setLastCommonParent(getBody());
                prevResult.setIndexInLastCommonParent(0);
            }
            if (nextLeaf != null) {
                nextResult = nextLeaf.getLastCommonParent(deletedNodes
                        .get(deletedNodes.size() - 1));
            } else {
                nextResult = new LastCommonParentResult();
                nextResult.setLastCommonParent(getBody());
                nextResult
                        .setIndexInLastCommonParent(getBody().getNbChildren());
            }

            if (prevResult.getLastCommonParentDepth() == nextResult
                    .getLastCommonParentDepth()) {
                if (deletedNodes.get(0).getParent()==deletedNodes.get(deletedNodes.size() - 1).getParent()
                        && prevResult.getLastCommonParent() == nextResult
                                .getLastCommonParent()) {
                    System.out.println("taking shortcut");
                    prevResult.setLastCommonParentDepth(prevResult
                            .getLastCommonParentDepth() + 1);

                } else {

                    // now THIS is tricky
                    double distancePrev = deletedNodes.get(0).getParent()
                            .getMatchRatio(prevResult.getLastCommonParent());
                    double distanceNext = deletedNodes.get(
                            deletedNodes.size() - 1).getParent().getMatchRatio(
                            nextResult.getLastCommonParent());

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
                    prevLeaf.getParent().splitUntill(
                            prevResult.getLastCommonParent(), prevLeaf, true);
                }
                prevLeaf = deletedNodes.remove(0).copyTree();
                prevLeaf.setParent(prevResult.getLastCommonParent());
                prevResult.getLastCommonParent().addChild(
                        prevResult.getIndexInLastCommonParent() + 1, prevLeaf);

            } else if (prevResult.getLastCommonParentDepth() < nextResult
                    .getLastCommonParentDepth()) {
                // Inserting at the back
                if (nextResult.isSplittingNeeded()) {
                    nextLeaf.getParent().splitUntill(
                            nextResult.getLastCommonParent(), nextLeaf, false);
                    // The place where to insert is shifted one place to the
                    // right
                    nextResult.setIndexInLastCommonParent(nextResult
                            .getIndexInLastCommonParent() + 1);
                }
                nextLeaf = deletedNodes.remove(deletedNodes.size() - 1)
                        .copyTree();
                nextLeaf.setParent(nextResult.getLastCommonParent());
                nextResult.getLastCommonParent().addChild(
                        nextResult.getIndexInLastCommonParent(), nextLeaf);
            } else {
                throw new IllegalStateException();
            }

        }
    }

}