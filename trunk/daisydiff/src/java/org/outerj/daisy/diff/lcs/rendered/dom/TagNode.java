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
package org.outerj.daisy.diff.lcs.rendered.dom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

public class TagNode extends Node implements Iterable<Node> {

    private List<Node> children = new ArrayList<Node>();

    private String qName;

    private Attributes attributes;

    public TagNode(TagNode parent, String qName, Attributes attributesarg) {
        super(parent);
        this.qName = qName;
        this.attributes = new AttributesImpl(attributesarg);
        if(qName.equalsIgnoreCase("a")){
            System.out.println("A tag:"+attributes.getValue(0));
        }
    }

    public void addChild(Node node) {
        if (node.getParent() != this)
            throw new IllegalStateException(
                    "The new child must have this node as a parent.");
        children.add(node);
    }

    public int getIndexOf(Node child) {
        return children.indexOf(child);
    }

    public void addChildBefore(int index, Node node) {
        if (node.getParent() != this)
            throw new IllegalStateException(
                    "The new child must have this node as a parent.");
        children.add(index, node);
    }

    public Node getChild(int i) {
        return children.get(i);
    }

    public Iterator<Node> iterator() {
        return children.iterator();
    }

    public int getNbChildren() {
        return children.size();
    }

    public String getQName() {
        return qName;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public boolean isSameTag(Object other) {
        if (other == null)
            return false;

        TagNode otherTagNode;
        try {
            otherTagNode = (TagNode) other;
        } catch (ClassCastException e) {
            System.out.println("ClassCastException");
            return false;
        }

        return getOpeningTag().equals(otherTagNode.getOpeningTag());
    }

    public String getOpeningTag() {
        String s = "<" + getQName();
        Attributes localAttributes = getAttributes();
        for (int i = 0; i < localAttributes.getLength(); i++) {
            s += " " + localAttributes.getQName(i) + "=\""
                    + localAttributes.getValue(i) + "\"";
        }
        return s += ">";
    }

    public String getEndTag() {
        return "</" + getQName() + ">";
    }

    public List<Node> getMinimalDeletedSet(int start) {
        List<Node> nodes = new ArrayList<Node>();

        if (children.size() == 0) {
            return nodes;
        }

        boolean hasNegativeChild = false;

        for (Node child : children) {
            List<Node> childrenChildren = child.getMinimalDeletedSet(start);
            nodes.addAll(childrenChildren);
            if (!hasNegativeChild
                    && !(childrenChildren.size() == 1 && childrenChildren
                            .contains(child))) {
                hasNegativeChild = true;
            }
        }
        if (!hasNegativeChild) {
            nodes.clear();
            nodes.add(this);
        }
        return nodes;
    }

    public String toString() {
        return getOpeningTag();
    }
    
    

}
