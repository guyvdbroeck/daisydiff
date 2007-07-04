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
package org.outerj.daisy.diff.lcs.classic;

import java.io.Writer;
import java.util.Locale;
import java.util.ResourceBundle;

public class TextDiffOutput implements DiffOutput {
    private Writer writer;

    private boolean markLines;

    private final StringBuilder contentLine = new StringBuilder();

    private final StringBuilder markLine = new StringBuilder();

    private boolean currentLineHasChangedText = false;

    private ResourceBundle bundle;

    public TextDiffOutput(Writer writer, boolean markLines) {
        this(writer, markLines, Locale.US);
    }

    public TextDiffOutput(Writer writer, boolean markLines, Locale locale) {
        this.writer = writer;
        this.markLines = markLines;
        bundle = ResourceBundle.getBundle("org/outerj/daisy/diff/messages",
                locale);
    }

    public void startLine(DiffLineType type) throws Exception {
        contentLine.setLength(0);
        if (type == DiffLineType.UNCHANGED) {
            contentLine.append("    ");
        } else if (type == DiffLineType.ADDED) {
            contentLine.append("+++ ");
        } else if (type == DiffLineType.REMOVED) {
            contentLine.append("--- ");
        }
        currentLineHasChangedText = false;
        if (markLines)
            markLine.setLength(0);
    }

    public void addUnchangedText(String text) throws Exception {
        if (markLines) {
            if (currentLineHasChangedText) {
                for (int i = 0; i < text.length(); i++)
                    markLine.append(' ');
            }
        }
        contentLine.append(text);
    }

    public void addChangedText(String text) throws Exception {
        if (markLines) {
            if (!currentLineHasChangedText) {
                currentLineHasChangedText = true;
                for (int i = 0; i < contentLine.length() - 4; i++)
                    // minus 4 for the margin width
                    markLine.append(' ');
            }
            for (int i = 0; i < text.length(); i++)
                markLine.append('^');
        }
        contentLine.append(text);
    }

    public void endLine() throws Exception {
        writer.write(contentLine.toString());
        writer.write('\n');
        if (markLines && currentLineHasChangedText) {
            if (markLine.length() > 0) {
                writer.write("    ");
                writer.write(markLine.toString());
                writer.write('\n');
            }
        }
    }

    public void skippedLines(int linesSkipped) throws Exception {
        writer.write("(" + linesSkipped + ' '
                + bundle.getString("equal-lines-skipped") + ")\n");
    }
}
