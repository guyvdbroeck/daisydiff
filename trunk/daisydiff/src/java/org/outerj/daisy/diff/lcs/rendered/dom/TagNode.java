package org.outerj.daisy.diff.lcs.rendered.dom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TagNode extends Node implements Iterable<Node> {

    public List<Node> children = new ArrayList<Node>();
    
    private String s;
    
    public TagNode(TagNode parent, String s) {
        super(parent);
        this.s = s;
    }
    
    public void addChild(Node node) {
        if(node.getParent()!=this)
            throw new IllegalStateException("The nw child must have this node as a parent.");
        children.add(node);
    }
    
    public Iterator<Node> iterator() {
        return children.iterator();
    }
    
    public String getText(){
        return s;
    }
    
    public boolean equals(Object other){
        if(other==null)
            return false;
        
        TagNode otherTagNode;
        try {
            otherTagNode = (TagNode)other;
        } catch (ClassCastException e) {
            System.out.println("ClassCastException");
            return false;
        }
        
        return getText().equals(otherTagNode.getText());
    }

}
