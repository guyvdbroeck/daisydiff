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
package org.outerj.daisy.diff.test.rendered;

import java.net.URL;

import org.outerj.daisy.diff.lcs.rendered.InputXMLReader;
import org.outerj.daisy.diff.lcs.rendered.LeafComparator;

public class TestRenderedComparator {

    public static void main(String[] args) throws Exception {
        new TestRenderedComparator(1, "daisydocs-2_0/374-cd/24-cd", 1, 4);
        new TestRenderedComparator(2, "wiki/476-cd", 3, 5);
        new TestRenderedComparator(3, "wiki/476-cd", 10, 11);
        new TestRenderedComparator(4, "wiki/476-cd", 20, 21);
        new TestRenderedComparator(5, "wiki/476-cd", 22, 23);
        new TestRenderedComparator(6, "daisydocs-2_0/13-cd", 2, 15);
        new TestRenderedComparator(7, "wiki/476-cd", 25, 26);
        new TestRenderedComparator(8, "daisydocs-2_0/373-cd/378-cd",1,5);
        new TestRenderedComparator(9, "wiki/476-cd", 30, 32);
        new TestRenderedComparator(10, "wiki/476-cd", 35, 36);
    }

    public TestRenderedComparator(int i, String document, int version1,
            int version2) throws Exception {

        LeafComparator leftContentHandler = new LeafComparator();
        LeafComparator rightContentHandler = new LeafComparator();

        InputXMLReader.readXML(new URL("http://cocoondev.org/" + document
                + "/version/" + version1 + "/part/SimpleDocumentContent/data"),
                leftContentHandler);
        InputXMLReader.readXML(new URL("http://cocoondev.org/" + document
                + "/version/" + version2 + "/part/SimpleDocumentContent/data"),
                rightContentHandler);

        RenderedDiffFileWriter.diff(

        "/home/guy/workspace/daisydiff/src/test/org/outerj/daisy/diff/test/html"
                + "/rendered" + i + ".html", leftContentHandler,
                rightContentHandler);
    }
}
