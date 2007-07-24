/*
 * Copyright 2007 Outerthought bvba and Schaubroeck nv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.outerj.daisy.diff.lcs.rendered;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.compare.rangedifferencer.PublicRangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import org.xml.sax.SAXException;

public class RenderedDiffer {

    private HtmlSaxRenderedDiffOutput output;

    public RenderedDiffer(HtmlSaxRenderedDiffOutput dm) {
        output = dm;
    }

    public void diff(LeafComparator leftComparator,
            LeafComparator rightComparator) throws SAXException {
        
        RangeDifference[] differences = RangeDifferencer.findDifferences(
                leftComparator, rightComparator);

        System.out.println("NBdifferences=" + differences.length);

        List<PublicRangeDifference> pdifferences = preProcess(differences,
                leftComparator);

        int currentIndexLeft = 0;
        int currentIndexRight = 0;

        for (PublicRangeDifference d : pdifferences) {

            if (d.leftStart() > currentIndexLeft) {
                handleClearPart(currentIndexLeft, d.leftStart(), currentIndexRight, d
                        .rightStart(), leftComparator, rightComparator);
            }
            if (d.leftLength() > 0) {
                rightComparator.markAsDeleted(d.leftStart(), d.leftEnd(),
                        leftComparator, d.rightStart());
            }
            rightComparator.markAsNew(d.rightStart(), d.rightEnd());

            currentIndexLeft = d.leftEnd();
            currentIndexRight = d.rightEnd();
        }
        if (currentIndexLeft < leftComparator.getRangeCount()) {
            handleClearPart(currentIndexLeft, leftComparator.getRangeCount(),
                    currentIndexRight, rightComparator.getRangeCount(),
                    leftComparator, rightComparator);
        }

        output.toHTML(rightComparator.getBody());
    }

    private void handleClearPart(int leftstart, int leftend, int rightstart,
            int rightend, LeafComparator leftComparator,
            LeafComparator rightComparator) {

        int i = rightstart;
        int j = leftstart;

        while (i < rightend) {

            rightComparator.compareTags(i, leftComparator.getLeaf(j));

            i++;
            j++;
        }

    }

    private List<PublicRangeDifference> preProcess(
            RangeDifference[] differences, LeafComparator leftComparator) {
        
        List<PublicRangeDifference> newRanges = new LinkedList<PublicRangeDifference>();

        for (int i = 0; i < differences.length; i++) {

            int leftStart = differences[i].leftStart();
            int leftEnd = differences[i].leftEnd();
            int rightStart = differences[i].rightStart();
            int rightEnd = differences[i].rightEnd();
            int kind = differences[i].kind();

            boolean connecting = true;

            while (connecting && i + 1 < differences.length
                    && differences[i + 1].kind() == kind) {
                if (differences[i + 1].leftStart() == leftEnd + 1
                        && differences[i + 1].rightStart() <= rightEnd + 1
                        && LeafComparator.isValidDelimiter(leftComparator
                                .getLeaf(leftEnd).getText().toCharArray()[0])) {
                    leftEnd = differences[i + 1].leftEnd();
                    rightEnd = differences[i + 1].rightEnd();
                    i++;
                    System.out.println("bridging 1");
                } else if (differences[i + 1].leftStart() == leftEnd + 2
                        && differences[i + 1].rightStart() == rightEnd + 2
                        && LeafComparator.isValidDelimiter(leftComparator
                                .getLeaf(leftEnd).getText().toCharArray()[0])
                        && LeafComparator
                                .isValidDelimiter(leftComparator.getLeaf(
                                        leftEnd + 1).getText().toCharArray()[0])) {
                    leftEnd = differences[i + 1].leftEnd();
                    rightEnd = differences[i + 1].rightEnd();
                    i++;
                    System.out.println("bridging 2");
                } else {
                    connecting = false;
                }
            }
            
            newRanges.add(new PublicRangeDifference(kind, rightStart, rightEnd
                    - rightStart, leftStart, leftEnd - leftStart));
        }

        return newRanges;
    }

}
