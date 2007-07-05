package org.outerj.daisy.diff.lcs.rendered.dom;

public class Node {
    
    private TagNode parent;
    
    public Node(TagNode parent){
        this.parent = parent;
        parent.addChild(this);
    }

    public TagNode getParent() {
        return parent;
    }
    
    
    
}
