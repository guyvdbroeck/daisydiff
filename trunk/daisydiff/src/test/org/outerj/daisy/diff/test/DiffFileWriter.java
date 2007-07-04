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

import java.io.File;

import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.outerj.daisy.diff.MarkupGenerator;
import org.outerj.daisy.diff.lcs.tag.TagComparator;
import org.outerj.daisy.diff.lcs.tag.TagDiffer;
import org.xml.sax.helpers.AttributesImpl;

public class DiffFileWriter {

    public static void diff(String file, TagComparator leftComparator,
            TagComparator rightComparator) throws Exception {
        StreamResult streamResult = new StreamResult(new File(file));
        SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory
                .newInstance();
        TransformerHandler serializer = tf.newTransformerHandler();

        serializer.setResult(streamResult);

        serializer.startDocument();
        AttributesImpl attrs = new AttributesImpl();
        serializer.startElement("", "html", "html", attrs);
        serializer.startElement("", "head", "head", attrs);
        serializer.endElement("", "head", "head");
        serializer.startElement("", "body", "body", attrs);

        MarkupGenerator dm = new MarkupGenerator(serializer);
        dm.printInfo();

        TagDiffer differ = new TagDiffer(dm);
        differ.parseNewDiff(leftComparator, rightComparator);

        serializer.endElement("", "body", "body");
        serializer.endElement("", "html", "html");
        serializer.endDocument();
    }

}
