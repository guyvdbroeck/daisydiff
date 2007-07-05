package org.outerj.daisy.diff.lcs.rendered;

import org.outerj.daisy.diff.lcs.tag.Atom;

public class MarkupTextAtom implements Atom {

    private String s;
    private Tag[] tags;
    
    public MarkupTextAtom(String s, Tag[] tags) {
        this.s=s;
        this.tags=tags;
    }
    
    public Tag[] getTags(){
        return tags;
    }
    
    public boolean equalsIdentifier(Atom other) {
        return false;
    }

    public String getFullText() {
        return s;
    }

    public String getIdentifier() {
        return s;
    }

    public String getInternalIdentifiers() {
        throw new IllegalArgumentException() ;
    }

    public boolean hasInternalIdentifiers() {
        return false;
    }

    public boolean isValidAtom(String s) {
        return s!=null && s.length()>0;
    }

}
