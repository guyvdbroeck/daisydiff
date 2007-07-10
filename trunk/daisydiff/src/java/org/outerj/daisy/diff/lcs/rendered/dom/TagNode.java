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
        try {
            System.out.println("inserting between "+children.get(index-1)+" and "+children.get(index));
        } catch (RuntimeException e) {
            System.out.println("exception caught");
        }
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

    public void splitUntill(TagNode parent, Node split, boolean includeLeft) {
        System.out.println("splitting "+parent+" at "+split);
        System.out.println("this is "+this);
        System.out.println("parent is "+getParent());
        if (parent != this) {
            TagNode part1 = new TagNode(null, getQName(), getAttributes());
            TagNode part2 = new TagNode(null, getQName(), getAttributes());
            part1.setParent(getParent());
            part2.setParent(getParent());

            int i = 0;
            while (i < children.size() && children.get(i) != split) {
                children.get(i).setParent(part1);
                part1.addChild(children.get(i));
                i++;
            }
            if (i < children.size()) {
                if (includeLeft) {
                    children.get(i).setParent(part1);
                    part1.addChild(children.get(i));
                } else {
                    children.get(i).setParent(part2);
                    part2.addChild(children.get(i));
                }
                i++;
            }
            while (i < children.size()) {
                children.get(i).setParent(part2);
                part2.addChild(children.get(i));
                i++;
            }
            if (part1.getNbChildren() > 0)
                getParent().addChildBefore(getParent().getIndexOf(this), part1);

            if (part2.getNbChildren() > 0)
                getParent().addChildBefore(getParent().getIndexOf(this), part2);

            getParent().removeChild(this);

            if (includeLeft)
                getParent().splitUntill(parent, part1, includeLeft);
            else
                getParent().splitUntill(parent, part2, includeLeft);
        }

    }

    private void removeChild(TagNode node) {
        children.remove(node);
    }

}
