package org.outerj.daisy.diff.lcs.rendered;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.compare.rangedifferencer.PublicRangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import org.outerj.daisy.diff.lcs.tag.DelimiterAtom;
import org.xml.sax.SAXException;


public class RenderedDiffer {

    private HtmlSaxRenderedDiffOutput output;
    
    public RenderedDiffer(HtmlSaxRenderedDiffOutput dm) {
        output = dm;
    }

    public void parseNewDiff(LeafComparator leftComparator, LeafComparator rightComparator) throws SAXException {
        RangeDifference[] differences = RangeDifferencer.findDifferences(
                leftComparator, rightComparator);

        System.out.println("NBdifferences="+differences.length);
        
        List<PublicRangeDifference> pdifferences = preProcess(differences,
                leftComparator);
        
        for(PublicRangeDifference d : pdifferences){
            System.out.println("making as new: "+ d.rightStart() + "-"+ d.rightEnd());
            rightComparator.markAsNew(d.rightStart(), d.rightEnd());
        }
        
        
        
        output.toHTML(rightComparator.getBody());
    }
    
    private void handleClearPart(int leftstart, int leftend, int rightstart, int rightend
            , LeafComparator leftComparator, LeafComparator rightComparator){
        
        
        
    }

    private List<PublicRangeDifference> preProcess(RangeDifference[] differences, LeafComparator leftComparator) {
        List<PublicRangeDifference> newRanges = new LinkedList<PublicRangeDifference>();

        for (int i = 0; i < differences.length; i++) {

            int leftStart = differences[i].leftStart();
            int leftEnd = differences[i].leftEnd();
            int rightStart = differences[i].rightStart();
            int rightEnd = differences[i].rightEnd();
            int kind = differences[i].kind();
            int temp = leftEnd;
//            boolean connecting = true;
//
//            while (connecting && i + 1 < differences.length
//                    && differences[i + 1].kind() == kind) {
//
//                int bridgelength = 0;
//
//                int nbtokens = Math.max((leftEnd - leftStart),
//                        (rightEnd - rightStart));
//                if (nbtokens > 5) {
//                    if (nbtokens > 10) {
//                        bridgelength = 3;
//                    } else
//                        bridgelength = 2;
//                }
//
//                while ((leftComparator.getAtom(temp) instanceof DelimiterAtom || (bridgelength-- > 0))
//                        && temp < differences[i + 1].leftStart()) {
//
//                    temp++;
//                }
//                if (temp == differences[i + 1].leftStart()) {
//                    leftEnd = differences[i + 1].leftEnd();
//                    rightEnd = differences[i + 1].rightEnd();
//                    temp = leftEnd;
//                    i++;
//                } else {
//                    connecting = false;
//                    if (!(leftComparator.getAtom(temp) instanceof DelimiterAtom)) {
//                        if (leftComparator.getAtom(temp).getFullText().equals(
//                                " "))
//                            throw new IllegalStateException(
//                                    "space found aiaiai");
//                    }
//                }
//            }
            newRanges.add(new PublicRangeDifference(kind, rightStart, rightEnd
                    - rightStart, leftStart, leftEnd - leftStart));
        }

        return newRanges;
    }

}
