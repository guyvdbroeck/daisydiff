package org.outerj.daisy.diff.lcs.rendered.dom;

public abstract class Node {
    
    private TagNode parent;
    
    public Node(TagNode parent){
        this.parent = parent;
        if(parent != null)
            parent.addChild(this);
    }

    public TagNode getParent() {
        return parent;
    }
    
    
    
}
