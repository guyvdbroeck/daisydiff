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
package org.outerj.daisy.diff.test;

import java.net.URL;

import org.outerj.daisy.diff.lcs.rendered.InputXMLReader;
import org.outerj.daisy.diff.lcs.rendered.LeafComparator;

public class TestRenderedComparator {

    public static void main(String[] args) throws Exception {
        new TestRenderedComparator(1);
        new TestRenderedComparator(2);
        new TestRenderedComparator(3);
        new TestRenderedComparator(4);
        new TestRenderedComparator(5);
    }

    public TestRenderedComparator(int i) throws Exception {

        LeafComparator leftContentHandler = new LeafComparator();
        LeafComparator rightContentHandler = new LeafComparator();

        if (i == 1) {
            InputXMLReader
                    .readXML(
                            new URL(
                                    "http://cocoondev.org/daisydocs-2_0/374-cd/24-cd/version/1/part/SimpleDocumentContent/data"),
                            leftContentHandler);
            InputXMLReader
                    .readXML(
                            new URL(
                                    "http://cocoondev.org/daisydocs-2_0/374-cd/24-cd/version/4/part/SimpleDocumentContent/data"),
                            rightContentHandler);
        } else if (i == 2) {
            InputXMLReader
                    .readXML(
                            new URL(
                                    "http://cocoondev.org/wiki/476-cd/version/3/part/SimpleDocumentContent/data"),
                            leftContentHandler);
            InputXMLReader
                    .readXML(
                            new URL(
                                    "http://cocoondev.org/wiki/476-cd/version/5/part/SimpleDocumentContent/data"),
                            rightContentHandler);
        } else if (i == 3) {
            InputXMLReader
                    .readXML(
                            new URL(
                                    "http://cocoondev.org/wiki/476-cd/version/10/part/SimpleDocumentContent/data"),
                            leftContentHandler);
            InputXMLReader
                    .readXML(
                            new URL(
                                    "http://cocoondev.org/wiki/476-cd/version/11/part/SimpleDocumentContent/data"),
                            rightContentHandler);
        } else if (i == 4) {
            InputXMLReader
                    .readXML(
                            new URL(
                                    "http://cocoondev.org/wiki/476-cd/version/20/part/SimpleDocumentContent/data"),
                            leftContentHandler);
            InputXMLReader
                    .readXML(
                            new URL(
                                    "http://cocoondev.org/wiki/476-cd/version/21/part/SimpleDocumentContent/data"),
                            rightContentHandler);
        } else if (i == 5) {
            InputXMLReader
                    .readXML(
                            new URL(
                                    "http://cocoondev.org/wiki/476-cd/version/22/part/SimpleDocumentContent/data"),
                            leftContentHandler);
            InputXMLReader
                    .readXML(
                            new URL(
                                    "http://cocoondev.org/wiki/476-cd/version/23/part/SimpleDocumentContent/data"),
                            rightContentHandler);
        }
        RenderedDiffFileWriter.diff(

        "/home/guy/workspace/daisydiff/src/test/org/outerj/daisy/diff/test/html"
                + "/rendered" + i + ".html", leftContentHandler,
                rightContentHandler);
    }
}
