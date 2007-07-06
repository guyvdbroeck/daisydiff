package org.outerj.daisy.diff.lcs.rendered.dom;

import java.util.List;

import org.outerj.daisy.diff.lcs.rendered.acestor.AncestorComparator;

public class TextNode extends Node{
    
    private String s;
    
    public TextNode(TagNode parent, String s){
        super(parent);
        
        this.s = s;
    }
    
    public String getText(){
        return s;
    }
    
    public boolean isSameText(Object other){
        if(other==null)
            return false;
        
        TextNode otherTextNode;
        try {
            otherTextNode = (TextNode)other;
        } catch (ClassCastException e) {
            System.out.println("ClassCastException");
            return false;
        }
//        System.out.println("comparing "+getText()+" and "+otherTextNode.getText());
//        System.out.println("returning "+getText().replace('\n', ' ')
//                .equals(otherTextNode.getText().replace('\n', ' ')));
        return getText().replace('\n', ' ')
                .equals(otherTextNode.getText().replace('\n', ' '));
    }
    
    private boolean isNew = false;
    
    public void markAsNew(){
        isNew = true;
    }
    
    public boolean isNew(){
        return isNew;
    }
    
    private boolean isChanged = false;
    
    public void compareTags(List<TagNode> other){
        
        AncestorComparator acthis = new AncestorComparator(this.getParentTree());
        AncestorComparator acother = new AncestorComparator(other);
        
        boolean changed = acthis.hasChanged(acother);
        
        if(changed){
            changes = acthis.getCompareTxt();
        }
        isChanged = changed;
    }
    
    public boolean isChanged(){
        return isChanged;
    }
    
    private String changes="";
    
    public String getChanges(){
        return changes;
    }

    public int getTagDistance(List<TagNode> other) {
        AncestorComparator acthis = new AncestorComparator(this.getParentTree());
        AncestorComparator acother = new AncestorComparator(other);
        
        acthis.hasChanged(acother);
        
        return acthis.getDistance(acother);
    }

    private boolean deleted = false;
    
    public void setDeleted() {
        deleted = true;
    }
    
    public boolean isDeleted(){
        return deleted;
    }

    public void setParent(TagNode parent) {
        this.parent = parent;
    }
    
}
