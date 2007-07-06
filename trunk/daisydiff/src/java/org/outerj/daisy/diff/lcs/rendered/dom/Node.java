package org.outerj.daisy.diff.lcs.rendered.dom;

import java.util.ArrayList;
import java.util.List;

public abstract class Node {
    
    protected TagNode parent;
    
    public Node(TagNode parent){
        this.parent = parent;
        if(parent != null)
            parent.addChild(this);
    }

    public TagNode getParent() {
        return parent;
    }
    
    public List<TagNode> getParentTree() {
        List<TagNode> parentTree = new ArrayList(5);
        if(getParent()!=null){
            parentTree.addAll(getParent().getParentTree());
            parentTree.add(getParent());
        }
        return parentTree;
    }
    
    
}
