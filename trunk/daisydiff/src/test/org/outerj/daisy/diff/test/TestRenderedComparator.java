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
        new TestRenderedComparator();
    }

    public TestRenderedComparator() throws Exception {
      
        LeafComparator leftContentHandler = new LeafComparator();
        LeafComparator rightContentHandler = new LeafComparator();

        InputXMLReader.readXML(new URL(
                "http://cocoondev.org/wiki/291-cd/version/18/part/SimpleDocumentContent/data")
        , leftContentHandler);
    
        InputXMLReader.readXML(new URL(
                "http://cocoondev.org/wiki/291-cd/version/24/part/SimpleDocumentContent/data")
        , rightContentHandler);
        
        RenderedDiffFileWriter.diff(
                "/home/guy/workspace/daisydiff/src/test/org/outerj/daisy/diff/test/html"
                        + "/rendered2.html", leftContentHandler, rightContentHandler);
    }
}
