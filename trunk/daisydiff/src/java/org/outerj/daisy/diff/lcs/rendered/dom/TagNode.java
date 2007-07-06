package org.outerj.daisy.diff.lcs.rendered.dom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.Attributes;

public class TagNode extends Node implements Iterable<Node> {

    private List<Node> children = new ArrayList<Node>();
    
    private String qName;

    private Attributes attributes;
    
    public TagNode(TagNode parent, String qName, Attributes attributes) {
        super(parent);
        this.qName = qName;
        this.attributes = attributes;
    }
    
    public void addChild(Node node) {
        if(node.getParent()!=this)
            throw new IllegalStateException("The new child must have this node as a parent.");
        children.add(node);
    }
    
    public int getIndexOf(Node child){
        return children.indexOf(child);
    }
    
    public void addChildBefore(int index, Node node) {
        if(node.getParent()!=this)
            throw new IllegalStateException("The new child must have this node as a parent.");
        children.add(index, node);
    }
    
    public Node getChild(int i){
        return children.get(i);
    }
    
    public Iterator<Node> iterator() {
        return children.iterator();
    }
    
    public int getNbChildren(){
        return children.size();
    }
    
    public String getQName(){
        return qName;
    }
    
    public Attributes getAttributes(){
        return attributes;
    }
    
    public boolean isSameTag(Object other){
        if(other==null)
            return false;
        
        TagNode otherTagNode;
        try {
            otherTagNode = (TagNode)other;
        } catch (ClassCastException e) {
            System.out.println("ClassCastException");
            return false;
        }
        
        return getOpeningTag().equals(otherTagNode.getOpeningTag());
    }

    public String getOpeningTag() {
        String s = "<" + getQName();
        Attributes localAttributes = getAttributes();
        for(int i=0; i<localAttributes.getLength();i++){
            s += " " + localAttributes.getQName(i)
            + "=\""+localAttributes.getValue(i)+"\"";
        }
        return s += ">";
    }
    
    public String getEndTag() {
        return "</" + getQName() + ">";
    }

}
