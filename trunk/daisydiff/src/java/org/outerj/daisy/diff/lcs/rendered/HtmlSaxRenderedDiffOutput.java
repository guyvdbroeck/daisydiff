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

import org.outerj.daisy.diff.lcs.rendered.dom.ImageNode;
import org.outerj.daisy.diff.lcs.rendered.dom.Node;
import org.outerj.daisy.diff.lcs.rendered.dom.TagNode;
import org.outerj.daisy.diff.lcs.rendered.dom.TextNode;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class HtmlSaxRenderedDiffOutput {

    private ContentHandler handler;
    private boolean addContentClassToTable;

    public HtmlSaxRenderedDiffOutput(ContentHandler handler) {
        this(handler,true);
    }
    
    public HtmlSaxRenderedDiffOutput(ContentHandler handler, boolean addContentClassToTable) {
        this.handler = handler;
        this.addContentClassToTable=addContentClassToTable;
    }

    private int removeID=1;
    private int addID=1;
    private int changeID=1;
    
    public void toHTML(TagNode node) throws SAXException {

        if(addContentClassToTable && node.getQName().equalsIgnoreCase("table") && node.getAttributes().getIndex("class")<0){
            AttributesImpl attrs=new AttributesImpl(node
                    .getAttributes());
            attrs.addAttribute("", "class", "class", "CDATA", "content");
            handler.startElement("", node.getQName(), node.getQName(), attrs);
        }else if(!node.getQName().equalsIgnoreCase("img")){
            handler.startElement("", node.getQName(), node.getQName(), node
                    .getAttributes());
        }
        
        boolean newStarted=false;
        boolean remStarted=false;
        boolean changeStarted=false;
        String changeTXT = "";
        
        for (Node child : node) {
            if (child instanceof TagNode) {
                if (newStarted) {
                    handler.endElement("", "span", "span");
                    newStarted=false;
                } else if (changeStarted) {
                    handler.endElement("", "span", "span");
                    changeStarted=false;
                } else if (remStarted) {
                    handler.endElement("", "span", "span");
                    remStarted=false;
                }
                toHTML(((TagNode) child));
            } else if (child instanceof TextNode) {
                TextNode textChild = (TextNode) child;

                if (newStarted && !textChild.isNew()) {
                    handler.endElement("", "span", "span");
                    newStarted=false;
                } else if (changeStarted && (!textChild.isChanged() 
                        || !textChild.getChanges().equals(changeTXT))) {
                    handler.endElement("", "span", "span");
                    changeStarted=false;
                } else if (remStarted && !textChild.isDeleted()) {
                    handler.endElement("", "span", "span");
                    remStarted=false;
                }
                
                
                if (!newStarted && textChild.isNew()) {
                    AttributesImpl attrs = new AttributesImpl();
                    attrs.addAttribute("", "class", "class", "CDATA",
                            "diff-tag-added");
                    attrs.addAttribute("", "id", "id", "CDATA",
                        "added"+addID);
                    attrs.addAttribute("", "title", "title", "CDATA",
                            "#added"+addID);
                    addID++;
                    handler.startElement("", "span", "span", attrs);
                    newStarted=true;
                } else if (!changeStarted && textChild.isChanged()) {
                    AttributesImpl attrs = new AttributesImpl();
                    attrs.addAttribute("", "class", "class", "CDATA",
                            "diff-tag-changed");
                    attrs.addAttribute("", "id", "id", "CDATA",
                            "changed"+changeID);
                    handler.startElement("", "span", "span", attrs);
                    
                    attrs = new AttributesImpl();
                    attrs.addAttribute("", "title", "title", "CDATA",
                            "#changed"+changeID);
                    handler.startElement("", "span", "span", attrs);

                    handler.characters(textChild.getChanges().toCharArray(), 0,
                            textChild.getChanges().length());

                    handler.endElement("", "span", "span");
                    
                    changeID++;
                    changeStarted=true;
                    changeTXT=textChild.getChanges();
                } else if (!remStarted && textChild.isDeleted()) {
                    AttributesImpl attrs = new AttributesImpl();
                    attrs.addAttribute("", "class", "class", "CDATA",
                            "diff-tag-removed");
                    attrs.addAttribute("", "id", "id", "CDATA",
                            "removed"+removeID);
                    attrs.addAttribute("", "title", "title", "CDATA",
                            "#removed"+removeID);
                    
                    handler.startElement("", "span", "span", attrs);
                    removeID++;
                    remStarted=true;
                }

                char[] chars = textChild.getText().toCharArray();
                if(textChild instanceof ImageNode){
                    ImageNode imgNode=(ImageNode)textChild;
                    AttributesImpl attrs = imgNode.getAttributes();
                    if(imgNode.isDeleted())
                        attrs.addAttribute("", "class", "class", "class", "removed");
                    else if(imgNode.isNew())
                        attrs.addAttribute("", "class", "class", "class", "added");
                    handler.startElement("", "img", "img", attrs);
                    handler.endElement("", "img", "img");
                }else
                    handler.characters(chars, 0, chars.length);

            }
        }
        
        if (newStarted) {
            handler.endElement("", "span", "span");
            newStarted=false;
        } else if (changeStarted) {
            handler.endElement("", "span", "span");
            changeStarted=false;
        } else if (remStarted) {
            handler.endElement("", "span", "span");
            remStarted=false;
        }
        
        if(!node.getQName().equalsIgnoreCase("img"))
            handler.endElement("", node.getQName(), node.getQName());

    }

}
