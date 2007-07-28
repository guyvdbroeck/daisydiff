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

import java.io.IOException;
import java.net.URL;

import org.outerj.daisy.diff.html.LeafComparator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Reads XML data and passes the parsed result to a {@link LeafComparator}.
 */
public class InputXMLReader {

    public static void readXML(URL url, LeafComparator handler)
            throws SAXException, IOException {
        readXML(new InputSource(url.openStream()), handler);
    }

    public static void readXML(InputSource s, LeafComparator handler)
            throws SAXException, IOException {
        XMLReader xr = XMLReaderFactory.createXMLReader();

        xr.setContentHandler(handler);
        xr.setErrorHandler(handler);

        xr.parse(s);
    }

}
