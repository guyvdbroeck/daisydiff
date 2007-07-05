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
        
        handler.startElement("", node.getQName(), node.getQName(), node.getAttributes());
        
        for(Node child : node){
            if(child instanceof TagNode){
                toHTML(((TagNode)child));
            }else if(child instanceof TextNode){
                TextNode textChild = (TextNode)child;
               
                if(textChild.isNew()){
                    AttributesImpl attrs = new AttributesImpl();
                    attrs.addAttribute("", "class", "class", "CDATA", "diff-tag-added");
                    
                    handler.startElement("", "span", "span", attrs);
                    
                }
                
                char[] chars = textChild.getText().toCharArray();
                handler.characters(chars, 0, chars.length);
            
                if(textChild.isNew()){
                    handler.endElement("", "span", "span");
                    
                }
            }   
        }
        
        handler.endElement("", node.getQName(), node.getQName());
        
    }

}
