/*
 * Copyright 2004 Outerthought bvba and Schaubroeck nv
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
package org.outerj.daisy.diff;

import java.io.IOException;
import java.io.StringReader;

import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import org.outerj.daisy.diff.lcs.html.HTMLDiffer;
import org.outerj.daisy.diff.lcs.html.HtmlSaxDiffOutput;
import org.outerj.daisy.diff.lcs.html.LeafComparator;
import org.outerj.daisy.diff.lcs.tag.TagComparator;
import org.outerj.daisy.diff.lcs.tag.TagDiffer;
import org.outerj.daisy.diff.lcs.tag.TagSaxDiffOutput;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class Diff {

    private Diff() {
    }

    /**
     * Diffs two html files, outputting the result to the specified consumer.
     */
    public static void diffHTML(String text1, String text2, ContentHandler consumer) throws SAXException, IOException{
        LeafComparator leftContentHandler = new LeafComparator();
        XMLReader xr1 = XMLReaderFactory.createXMLReader();
        xr1.setContentHandler(leftContentHandler);
        xr1.setErrorHandler(leftContentHandler);
        xr1.parse(new InputSource(new StringReader(text1)));
        
        LeafComparator rightContentHandler = new LeafComparator();
        XMLReader xr2 = XMLReaderFactory.createXMLReader();
        xr2.setContentHandler(rightContentHandler);
        xr2.setErrorHandler(rightContentHandler);
        xr2.parse(new InputSource(new StringReader(text2)));
        
        HtmlSaxDiffOutput output = new HtmlSaxDiffOutput(
                consumer);
        HTMLDiffer differ = new HTMLDiffer(output);
        differ.diff(leftContentHandler, rightContentHandler);
    }
    
    /**
     * Diffs two html files word for word as source, outputting the result 
     * to the specified consumer.
     * @throws Exception 
     */
    public static void diffTag(String text1, String text2, ContentHandler consumer) throws Exception{
        TagComparator left=new TagComparator(text1);
        TagComparator right=new TagComparator(text2);
        
        TagSaxDiffOutput output = new TagSaxDiffOutput(consumer);
        TagDiffer differ=new TagDiffer(output);
        differ.diff(left, right);
    }

    /**
     * Diffs two texts, outputting the result to the specified DiffOutput instance.
     *
     * @param contextLineCount specify -1 to include complete text
     * @throws Exception the differencing itself should normally not throw exceptions, but the
     *                   methods on DiffOutput can.
     */
    public static void diff(String text1, String text2, DiffOutput output, int contextLineCount) throws Exception {
        TextComparator leftComparator = new TextComparator(text1);
        TextComparator rightComparator = new TextComparator(text2);

        RangeDifference[] differences = RangeDifferencer.findDifferences(leftComparator, rightComparator);

        int pos = 0; // the position (line) in the left input
        if (differences.length > 0) {
            int diffIndex = 0;

            int leftLineCount = leftComparator.getRangeCount();
            while (diffIndex < differences.length && pos < leftLineCount) {
                RangeDifference diff = differences[diffIndex];

                if (diff.kind() == RangeDifference.CHANGE) {
                    int nextChangedLine = diff.leftStart();

                    // output contextLineCount number of lines (if available) or all lines if contextLineCount == -1
                    if (pos != 0) { // at start of file, skip immediately to first changes
                        int beginContextEndPos = pos + contextLineCount;
                        while ((pos < beginContextEndPos || contextLineCount == -1) && pos < nextChangedLine) {
                            output.startLine(DiffLineType.UNCHANGED);
                            output.addUnchangedText(leftComparator.getLine(pos));
                            output.endLine();
                            pos++;
                        }
                    }

                    // skip a number of lines
                    if (contextLineCount >= 0) {
                        int endContextStartPos = nextChangedLine - contextLineCount;
                        if (endContextStartPos > pos + 1) { // the +1 is to avoid skipping just one line
                            output.skippedLines(endContextStartPos - pos);
                            pos = endContextStartPos;
                        }
                    }

                    // output contextLineCount number of lines
                    while (pos < nextChangedLine) {
                        output.startLine(DiffLineType.UNCHANGED);
                        output.addUnchangedText(leftComparator.getLine(pos));
                        output.endLine();
                        pos++;
                    }

                    StringBuilder leftBlock = null;
                    StringBuilder rightBlock = null;
                    if (diff.leftLength() > 0 && diff.rightLength() > 0) {
                        leftBlock = concatLines(leftComparator, diff.leftStart(), diff.leftLength());
                        rightBlock = concatLines(rightComparator, diff.rightStart(), diff.rightLength());
                    }

                    if (leftBlock == null) {
                        for (int i = 0; i < diff.leftLength(); i++) {
                            int currentLine = diff.leftStart() + i;
                            output.startLine(DiffLineType.REMOVED);
                            output.addUnchangedText(leftComparator.getLine(currentLine));
                            output.endLine();
                        }
                    } else {
                        diffBlock(leftBlock, rightBlock, output, DiffLineType.REMOVED);
                    }

                    if (leftBlock == null) {
                        for (int i = 0; i < diff.rightLength(); i++) {
                            int currentLine = diff.rightStart() + i;
                            output.startLine(DiffLineType.ADDED);
                            output.addUnchangedText(rightComparator.getLine(currentLine));
                            output.endLine();
                        }
                    } else {
                        diffBlock(rightBlock, leftBlock, output, DiffLineType.ADDED);
                    }
                }


                pos = differences[diffIndex].leftEnd();
                diffIndex++;
            }

            // output any remaining lines
            int endPos = pos;
            while (pos < leftLineCount && (contextLineCount == -1 || pos < endPos + contextLineCount)) {
                output.startLine(DiffLineType.UNCHANGED);
                output.addUnchangedText(leftComparator.getLine(pos));
                output.endLine();
                pos++;
            }
            if (pos < leftLineCount) {
                output.skippedLines(leftLineCount - pos);
            }
        }
    }

    private static StringBuilder concatLines(TextComparator comparator, int start, int count) {
        int totalLinesLength = 0;
        for (int i = 0; i < count; i++)
            totalLinesLength += comparator.getLine(start + i).length() + 1;

        StringBuilder result = new StringBuilder(totalLinesLength);
        for (int i = 0; i < count; i++) {
            if (i > 0)
                result.append("\n");
            result.append(comparator.getLine(start + i));
        }

        return result;
    }

    private static void diffBlock(StringBuilder block1, StringBuilder block2, DiffOutput output, DiffLineType diffLineType) throws Exception {
        BlockComparator leftBlockComparator = new BlockComparator(block1);
        BlockComparator rightBlockComparator = new BlockComparator(block2);
        RangeDifference[] lineDiffs =  RangeDifferencer.findDifferences(leftBlockComparator, rightBlockComparator);

        int pos = 0;
        RangeDifference diff = null;
        output.startLine(diffLineType);

        for (int i = 0; i < lineDiffs.length; i++) {
            diff = lineDiffs[i];

            int left = diff.leftStart();
            if (pos < left) {
                String[] strings = leftBlockComparator.substringSplitted(pos, left);
                for (int d = 0; d < strings.length; d++) {
                    if (strings[d].equals("\n")) {
                        output.endLine();
                        output.startLine(diffLineType);
                    } else {
                        output.addUnchangedText(strings[d]);
                    }
                }
            }

            if (diff.leftLength() > 0) {
                String[] strings = leftBlockComparator.substringSplitted(left, diff.leftEnd());
                for (int d = 0; d < strings.length; d++) {
                    if (strings[d].equals("\n")) {
                        output.endLine();
                        output.startLine(diffLineType);
                    } else {
                        output.addChangedText(strings[d]);
                    }
                }
            }

            pos = diff.leftEnd();
        }

        if (diff == null || diff.leftEnd() < leftBlockComparator.getRangeCount()) {
            int start = 0;
            if (diff != null)
                start = diff.leftEnd();
            String[] strings = leftBlockComparator.substringSplitted(start);
            for (int d = 0; d < strings.length; d++) {
                if (strings[d].equals("\n")) {
                    output.endLine();
                    output.startLine(diffLineType);
                } else {
                    output.addUnchangedText(strings[d]);
                }
            }
            output.endLine();
        } else {
            output.endLine();
        }
    }

}
