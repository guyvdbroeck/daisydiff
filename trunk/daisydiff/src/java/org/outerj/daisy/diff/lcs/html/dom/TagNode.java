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
package org.outerj.daisy.diff.lcs.html.dom;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.outerj.daisy.diff.lcs.html.helper.TextOnlyComparator;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
/**
 * Node that can contain other nodes. Represents an HTML tag. 
 */
public class TagNode extends Node implements Iterable<Node> {

    private List<Node> children = new ArrayList<Node>();

    private String qName;

    private Attributes attributes;

    public TagNode(TagNode parent, String qName, Attributes attributesarg) {
        super(parent);
        this.qName = qName;
        attributes = new AttributesImpl(attributesarg);
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

    public void addChild(int index, Node node) {
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

    @Override
    public List<Node> getMinimalDeletedSet(int start) {

        List<Node> nodes = new ArrayList<Node>();

        if (children.size() == 0)
            return nodes;

        boolean hasNotDeletedDescendant = false;

        for (Node child : this) {
            List<Node> childrenChildren = child.getMinimalDeletedSet(start);
            nodes.addAll(childrenChildren);
            if (!hasNotDeletedDescendant
                    && !(childrenChildren.size() == 1 && childrenChildren
                            .contains(child))) {
                // This child is not entirely deleted
                hasNotDeletedDescendant = true;
            }
        }
        if (!hasNotDeletedDescendant) {
            nodes.clear();
            nodes.add(this);
        }
        return nodes;
    }

    @Override
    public String toString() {
        return getOpeningTag();
    }

    public void splitUntill(TagNode parent, Node split, boolean includeLeft) {
//        System.out.println("splitting " + parent + " at " + split);
//        System.out.println("this is " + this);
//        System.out.println("parent is " + getParent());
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
                getParent().addChild(getParent().getIndexOf(this), part1);

            if (part2.getNbChildren() > 0)
                getParent().addChild(getParent().getIndexOf(this), part2);

            getParent().removeChild(this);

            if (includeLeft)
                getParent().splitUntill(parent, part1, includeLeft);
            else
                getParent().splitUntill(parent, part2, includeLeft);
        }

    }

    private void removeChild(Node node) {
        children.remove(node);
    }
    
    private void removeChild(int i) {
        children.remove(i);
    }
    
    @Override
    public void detectIgnorableWhiteSpace() {
        boolean blockBefore=true;
        
        for (int i = 0; i < getNbChildren(); i++) {
            getChild(i).detectIgnorableWhiteSpace();
            
            //Detect a streak of whitespace and remove it if needed
            if(getChild(i) instanceof TextNode && ((TextNode)getChild(i)).isWhitespace()){
                int start=i;
                while (i<getNbChildren() && getChild(i) instanceof TextNode && ((TextNode)getChild(i)).isWhitespace()) {
                    i++;
                }
                if(blockBefore && (i>=getNbChildren() || isBlockLevel(getChild(i)))){
                    markAsIgnorable(start,i);
                }
                if(i>=getNbChildren())
                    return;
                //Make sure the next iteration of the for loop has element i as its element
                i--;
            }else{
                //Detect if this is a blocklevel tag
                if(isBlockLevel(children.get(i))){
                    //There is a block lvl element before the next child;
                    blockBefore=true;
                }else{
                    //There is text or an inline element before the next child
                    blockBefore=false;
                }
            }
        }
    }

    public void removeIgnorableWhiteSpace() {
        List<Node> toRemove = new ArrayList<Node>();
        for(Node child:this){
            try {
                TextNode textChild=(TextNode)child;
                if(textChild.isIgnorable()){
                    toRemove.add(child);
                }
            } catch (ClassCastException e) {}
            try {
                TagNode tagChild=(TagNode)child;
                tagChild.removeIgnorableWhiteSpace();
            } catch (ClassCastException e) {}
        }
        children.removeAll(toRemove);
    }
    
    private void markAsIgnorable(int start, int end) {
        for(int i=start;i<end;i++){
            TextNode child=(TextNode)getChild(i);
            child.markAsIgnorable();
        }
    }

    private static Set<String> blocks = new HashSet<String>();
    {
        blocks.add("html");
        blocks.add("body");
        blocks.add("p");
        blocks.add("blockquote");
        blocks.add("h1");
        blocks.add("h2");
        blocks.add("h3");
        blocks.add("h4");
        blocks.add("h5");
        blocks.add("pre");
        blocks.add("div");
        blocks.add("ul");
        blocks.add("ol");
        blocks.add("li");
        blocks.add("table");
        blocks.add("tbody");
        blocks.add("tr");
        blocks.add("td");
        blocks.add("th");
        blocks.add("br");
    }

    public static boolean isBlockLevel(Node node) {
        try {
            TagNode tagnode = (TagNode) node;
            return blocks.contains(tagnode.getQName().toLowerCase());
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public Node copyTree() {
        TagNode newThis=new TagNode(null,getQName(),new AttributesImpl(getAttributes()));
        for(Node child:this){
            Node newChild=child.copyTree();
            newChild.setParent(newThis);
            newThis.addChild(newChild);
        }
        return newThis;
    }

    public double getMatchRatio(TagNode other) {
        TextOnlyComparator txtComp=new TextOnlyComparator(other);
        return txtComp.getMatchRatio(new TextOnlyComparator(this));
    }

}
