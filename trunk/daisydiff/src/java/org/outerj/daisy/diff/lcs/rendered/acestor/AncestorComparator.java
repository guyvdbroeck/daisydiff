package org.outerj.daisy.diff.lcs.rendered.acestor;

import java.util.List;

import org.eclipse.compare.rangedifferencer.IRangeComparator;
import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import org.outerj.daisy.diff.lcs.rendered.dom.TagNode;

public class AncestorComparator implements IRangeComparator{
    
    private List<TagNode> ancestors;

    public AncestorComparator(List<TagNode> ancestors){
        this.ancestors = ancestors;
    }
    
    public int getRangeCount() {
        return ancestors.size();
    }

    public boolean rangesEqual(int owni, IRangeComparator otherComp, int otheri) {
        AncestorComparator other;
        try {
            other = (AncestorComparator)otherComp;
        } catch (ClassCastException e) {
            return false;
        }
        
        return other.getAncestor(otheri).isSameTag(getAncestor(owni));
    }

    public boolean skipRangeComparison(int arg0, int arg1, IRangeComparator arg2) {
        return false;
    }
    
    public TagNode getAncestor(int i){
        return ancestors.get(i);
    }

    private String compareTxt = "";
    
    public String getCompareTxt() {
        return compareTxt;
    }

    public boolean hasChanged(AncestorComparator other){
        
        RangeDifference[] differences = RangeDifferencer.findDifferences(
                other, this);
        
        if(differences.length==0)
            return false;
        
        compareTxt="";
        
        for (RangeDifference d : differences) {
            if (d.leftLength() == 0 && d.rightLength() > 0) {
                if (d.rightLength()==1) {
                    compareTxt += "A new "
                            + this.getAncestor(d.rightStart()).getOpeningTag()
                            + " tag was added. \n";
                }else if (d.rightLength()==2) {
                    compareTxt += "A new "
                        + this.getAncestor(d.rightStart()).getOpeningTag()
                        + " ";
                    compareTxt += "and "
                            + this.getAncestor(d.rightEnd()-1).getOpeningTag()
                            + " tag ";
                    compareTxt += " were added. \n";
                }else{
                    compareTxt += "A new "
                        + this.getAncestor(d.rightStart()).getOpeningTag();
                    for (int i = d.rightStart() + 1; i < d.rightEnd() - 1; i++) {
                        compareTxt += ", "
                                + this.getAncestor(i).getOpeningTag();
                    }
                    compareTxt += " and "
                            + this.getAncestor(d.rightEnd()-1).getOpeningTag()
                            + " tag ";
                    compareTxt += " were added. \n";
                }
             } else if (d.rightLength() == 0) {
                 if (d.leftLength()==1) {
                     compareTxt += "An old "
                             + other.getAncestor(d.leftStart()).getOpeningTag()
                             + " tag was removed. \n";
                 }else if (d.leftLength()==2) {
                     compareTxt += "An old "
                         + other.getAncestor(d.leftStart()).getOpeningTag()
                         + " ";
                     compareTxt += "and "
                             + other.getAncestor(d.leftEnd()-1).getOpeningTag()
                             + " tag ";
                     compareTxt += " were removed. \n";
                 }else{
                     compareTxt += "An old "
                         + other.getAncestor(d.leftStart()).getOpeningTag();
                     for (int i = d.leftStart() + 1; i < d.leftEnd() - 1; i++) {
                         compareTxt += ", "
                                 + other.getAncestor(i).getOpeningTag();
                     }
                     compareTxt += " and "
                             + other.getAncestor(d.leftEnd()-1).getOpeningTag()
                             + " tag ";
                     compareTxt += " were removed.\n";
                 }
            } else {
                if (d.leftLength()==1) {
                    compareTxt += "An old "
                            + other.getAncestor(d.leftStart()).getOpeningTag()
                            + " tag was replaced by ";
                }else if (d.leftLength()==2) {
                    compareTxt += "An old "
                        + other.getAncestor(d.leftStart()).getOpeningTag()
                        + " ";
                    compareTxt += "and "
                            + other.getAncestor(d.leftEnd()-1).getOpeningTag()
                            + " tag ";
                    compareTxt += " were replaced by ";
                }else{
                    compareTxt += "An old "
                        + other.getAncestor(d.leftStart()).getOpeningTag();
                    for (int i = d.leftStart() + 1; i < d.leftEnd() - 1; i++) {
                        compareTxt += ", "
                                + other.getAncestor(i).getOpeningTag();
                    }
                    compareTxt += " and "
                            + other.getAncestor(d.leftEnd()-1).getOpeningTag()
                            + " tag ";
                    compareTxt += " were replaced by ";
                }
                
                if (d.rightLength()==1) {
                    compareTxt += "a new "
                            + this.getAncestor(d.rightStart()).getOpeningTag()
                            + " tag. \n";
                }else if (d.rightLength()==2) {
                    compareTxt += "a new "
                        + this.getAncestor(d.rightStart()).getOpeningTag()
                        + " tag ";
                    compareTxt += "and "
                            + this.getAncestor(d.rightEnd()-1).getOpeningTag()
                            + " tag ";
                    compareTxt += ". \n";
                }else{
                    compareTxt += "a new "
                        + this.getAncestor(d.rightStart()).getOpeningTag();
                    for (int i = d.rightStart() + 1; i < d.rightEnd() - 1; i++) {
                        compareTxt += ", "
                                + this.getAncestor(i).getOpeningTag() + "";
                    }
                    compareTxt += " and "
                            + this.getAncestor(d.rightEnd()-1).getOpeningTag()
                            + " tag ";
                    compareTxt += ". \n";
                }
            }
        }
        
        
        
        return true;

    }

    public int getDistance(AncestorComparator other) {
        RangeDifference[] differences = RangeDifferencer.findDifferences(
                other, this);
        
        int distance=0;
        
        for (RangeDifference d : differences) {
            distance+=d.rightLength()+d.leftLength();
        }
        return distance;
    }
    
}
