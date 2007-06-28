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
package org.outerj.daisy.diff.test;

import org.outerj.daisy.diff.*;
import junit.framework.TestCase;

import java.io.*;

public class CompareTest extends TestCase {
    public void testDiff() throws Exception {
        String result;

        result = doDiff(readResource("text1.txt"), readResource("text2.txt"), true, -1);
        assertEquals(readResource("output1.txt"), result);

        result = doDiff(readResource("text1.txt"), readResource("text2.txt"), false, 1);
        assertEquals(readResource("output2.txt"), result);

        result = doDiff(readResource("text3.txt"), readResource("text4.txt"), false, 1);
        assertEquals(readResource("output3.txt"), result);

        result = doDiff(readResource("text3.txt"), readResource("text4.txt"), false, 2);
        assertEquals(readResource("output4.txt"), result);

        result = doDiff(readResource("text5.txt"), readResource("text6.txt"), true, -1);
        assertEquals(readResource("output5.txt"), result);
    }

    String doDiff(String text1, String text2, boolean markLines, int contextLines) throws Exception {
        StringWriter writer = new StringWriter();
        Diff.diff(text1, text2, new TextDiffOutput(writer, markLines), contextLines);
        return writer.toString();
    }

    String readResource(String name) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("org/outerj/daisy/diff/test/" + name);
        Reader reader = new InputStreamReader(is, "UTF-8");
        BufferedReader bufferedReader = new BufferedReader(reader);

        StringBuilder buffer = new StringBuilder();
        int c = bufferedReader.read();
        while (c != -1) {
            buffer.append((char)c);
            c = bufferedReader.read();
        }

        return buffer.toString();
    }
}
