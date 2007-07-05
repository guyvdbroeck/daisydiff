package org.outerj.daisy.diff.lcs.rendered.dom;

public class TextNode extends Node{
    
    private String s;
    
    public TextNode(TagNode parent, String s){
        super(parent);
        
        this.s = s;
    }
    
    public String getText(){
        return s;
    }
    
    public boolean equals(Object other){
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
    
}
