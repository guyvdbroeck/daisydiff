package org.outerj.daisy.diff.lcs.rendered.dom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TagNode implements Iterable<Node> {

    public List<Node> children = new ArrayList<Node>();
    
    public void addChild(Node node) {
        if(node.getParent()!=this)
            throw new IllegalStateException("The nw child must have this node as a parent.");
        children.add(node);
    }
    
    public Iterator<Node> iterator() {
        return children.iterator();
    }

}
