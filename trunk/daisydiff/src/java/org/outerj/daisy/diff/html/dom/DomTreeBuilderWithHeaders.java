/*
 * Copyright 2007 Guy Van den Broeck
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
package org.outerj.daisy.diff.html.dom;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class DomTreeBuilderWithHeaders extends DomTreeBuilder {

    private ContentHandler handler;

    private List<String> closingQname = new ArrayList<String>();
    private List<String> closingLocalName = new ArrayList<String>();
    private List<String> closingUri = new ArrayList<String>();

    private String[] cssPath;
    private String[] jsPath;

    private boolean cssWritten=false;

    private boolean insideIgnoredTag=false;
    
    public DomTreeBuilderWithHeaders(ContentHandler handler, String[] cssPath, String[] jsPath) {
        this.handler = handler;
        this.cssPath = cssPath;
        this.jsPath = jsPath;
    }

    private void writeCSS(String uri) throws SAXException {
        for(String css:cssPath){
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute(uri, "href", "href", "CDATA", css);
            attrs.addAttribute(uri, "type", "type", "CDATA", "text/css");
            attrs.addAttribute(uri, "rel", "rel", "CDATA", "stylesheet");
            handler.startElement(uri, "link", "link", attrs);
            handler.endElement(uri, "link", "link");
        }
        cssWritten=true;
    }

    private void writeJS(String uri) throws SAXException{
        for(String js:jsPath){
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute(uri, "src", "src", "CDATA", js);
            attrs.addAttribute(uri, "type", "type", "CDATA", "text/javascript");
            handler.startElement(uri, "script", "script", attrs);
            handler.endElement(uri, "script", "script");
        }
    }

    @Override
    public void startElement(String uri, String localName, String name,
            Attributes attributes) throws SAXException {
        if(documentStarted && ! bodyStarted){
            if(name.equals("body")){
                if(!cssWritten){
                    handler.startElement(uri, "head", "head", new AttributesImpl());
                    writeCSS(uri);
                    handler.endElement(uri, "head", "head");
                }
                handler.startElement(uri, localName, name, attributes);
                writeJS(uri);
            }else if(!insideIgnoredTag && !name.equals("script")){
                handler.startElement(uri, localName, name, attributes);
            }else{
                insideIgnoredTag = true;
            }

        }
        super.startElement(uri, localName, name, attributes);
    }
    
    @Override
    public void endElement(String uri, String localName, String name)
    throws SAXException {
        if(name.equalsIgnoreCase("head"))
            writeCSS(uri);
        super.endElement(uri, localName, name);
        if(documentStarted && ! bodyStarted){
            if(!insideIgnoredTag)
                handler.endElement(uri, localName, name);
            else if(name.equals("script"))
                insideIgnoredTag=false;
              
        }
        if(bodyEnded)
            storeTagForLater(uri, localName, name);

    }

    private void storeTagForLater(String uri, String localname, String qname) {
        closingUri.add(uri);
        closingLocalName.add(localname);
        closingQname.add(qname);
    }

    @Override
    public void characters(char[] ch, int start, int length)
    throws SAXException {
        if(documentStarted && ! bodyStarted && !insideIgnoredTag){
            handler.characters(ch, start, length);
        }
        super.characters(ch, start, length);
    }

    public void writeClosingTag() throws SAXException{
        for(int i=0;i<closingUri.size();i++)
            handler.endElement(closingUri.get(i), closingLocalName.get(i), closingQname.get(i));
    }

}
