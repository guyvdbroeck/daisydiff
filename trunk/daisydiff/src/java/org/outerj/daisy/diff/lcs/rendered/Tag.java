package org.outerj.daisy.diff.lcs.rendered;

import org.xml.sax.Attributes;

public class Tag {

    private Attributes attributes;
    private String qName;
    
    public Tag(String qName, Attributes attributes) {
        this.qName = qName;
        this.attributes =  attributes;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public String getQName() {
        return qName;
    }
    
    public String toString(){
        String s = "<"+getQName();
        for(int i=0;i<attributes.getLength();i++){
            s+= " "+attributes.getQName(i)+"=\""+
                attributes.getValue(i)+"\"";
        }
        return s+">";
    }
    
    public String toBasicString(){
        return "<"+getQName()+">";
    }
    
    
    
}
