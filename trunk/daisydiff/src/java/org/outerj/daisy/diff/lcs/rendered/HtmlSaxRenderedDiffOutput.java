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
package org.outerj.daisy.diff.lcs.rendered;

import javax.xml.transform.sax.TransformerHandler;

import org.outerj.daisy.diff.lcs.rendered.dom.Node;
import org.outerj.daisy.diff.lcs.rendered.dom.TagNode;
import org.outerj.daisy.diff.lcs.rendered.dom.TextNode;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class HtmlSaxRenderedDiffOutput {

    private TransformerHandler handler;

    public HtmlSaxRenderedDiffOutput(TransformerHandler handler) {
        this.handler = handler;
    }

    public void toHTML(TagNode node) throws SAXException {

        handler.startElement("", node.getQName(), node.getQName(), node
                .getAttributes());

        for (Node child : node) {
            if (child instanceof TagNode) {
                toHTML(((TagNode) child));
            } else if (child instanceof TextNode) {
                TextNode textChild = (TextNode) child;

                if (textChild.isNew()) {
                    AttributesImpl attrs = new AttributesImpl();
                    attrs.addAttribute("", "class", "class", "CDATA",
                            "diff-tag-added");

                    handler.startElement("", "span", "span", attrs);
                } else if (textChild.isChanged()) {
                    AttributesImpl attrs = new AttributesImpl();
                    attrs.addAttribute("", "class", "class", "CDATA",
                            "diff-tag-changed");

                    attrs.addAttribute("", "href", "href", "CDATA", "#");

                    handler.startElement("", "a", "a", attrs);
                } else if (textChild.isDeleted()) {
                    AttributesImpl attrs = new AttributesImpl();
                    attrs.addAttribute("", "class", "class", "CDATA",
                            "diff-tag-removed");

                    handler.startElement("", "span", "span", attrs);
                }

                char[] chars = textChild.getText().toCharArray();
                handler.characters(chars, 0, chars.length);

                if (textChild.isNew()) {
                    handler.endElement("", "span", "span");

                } else if (textChild.isChanged()) {
                    AttributesImpl attrs = new AttributesImpl();

                    handler.startElement("", "span", "span", attrs);

                    handler.characters(textChild.getChanges().toCharArray(), 0,
                            textChild.getChanges().length());

                    handler.endElement("", "span", "span");

                    handler.endElement("", "a", "a");
                } else if (textChild.isDeleted()) {
                    handler.endElement("", "span", "span");

                }
            }
        }

        handler.endElement("", node.getQName(), node.getQName());

    }

}
