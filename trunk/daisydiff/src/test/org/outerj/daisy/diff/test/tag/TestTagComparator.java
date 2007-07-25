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
package org.outerj.daisy.diff.test.tag;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.outerj.daisy.diff.lcs.tag.TagComparator;

public class TestTagComparator {

    public static void main(String[] args) throws Exception {
        new TestTagComparator();
    }

    public TestTagComparator() throws Exception {
        StringBuilder leftsb = new StringBuilder();
        String left = readResource("gsoc.txt");

        leftsb.append(left);

        StringBuilder rightsb = new StringBuilder();
        String right = readResource("gsocchanged.txt");

        rightsb.append(right);

        TagComparator lefttc = new TagComparator(leftsb);
        TagComparator righttc = new TagComparator(rightsb);

        TagDiffFileWriter.diff(
                "/home/guy/workspace/daisydiff/src/test/org/outerj/daisy/diff/test/html"
                        + "/tag-word2.html", lefttc, righttc);

    }

    private String readResource(String name) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(
                "org/outerj/daisy/diff/test/txt/" + name);
        Reader reader = new InputStreamReader(is, "UTF-8");
        BufferedReader bufferedReader = new BufferedReader(reader);

        StringBuilder buffer = new StringBuilder();
        int c = bufferedReader.read();
        while (c != -1) {
            buffer.append((char) c);
            c = bufferedReader.read();
        }

        return buffer.toString();
    }
}
