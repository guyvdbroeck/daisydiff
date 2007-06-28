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

import org.eclipse.compare.rangedifferencer.IRangeComparator;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Comparator for comparing text on a line-by-line basis.
 */
public class TextComparator implements IRangeComparator {
    private String[] lines;

    /**
     * @param text the input text as one big string (thus containing newlines)
     */
    public TextComparator(String text) {
        BufferedReader reader = new BufferedReader(new StringReader(text));

        ArrayList lines = new ArrayList(50);
        try {
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Unexpected: got exception while reading from String object.", e);
        }
        this.lines = (String[])lines.toArray(new String[lines.size()]);
    }

    public int getRangeCount() {
        return lines.length;
    }

    public boolean rangesEqual(int thisIndex, IRangeComparator other, int otherIndex) {
        String thisLine = getLine(thisIndex);
        String otherLine = ((TextComparator)other).getLine(otherIndex);
        return thisLine.equals(otherLine);
    }

    public boolean skipRangeComparison(int length, int maxLength, IRangeComparator other) {
        return false;
    }

    public String getLine(int index) {
        return index < lines.length ? lines[index] : "";
    }
}
