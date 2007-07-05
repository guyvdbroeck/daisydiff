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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.rangedifferencer.IRangeComparator;
import org.outerj.daisy.diff.lcs.rendered.dom.BodyNode;
import org.outerj.daisy.diff.lcs.rendered.dom.TagNode;
import org.outerj.daisy.diff.lcs.rendered.dom.TextNode;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class LeafComparator extends DefaultHandler implements IRangeComparator
{

    private List<TextNode> leafs = new ArrayList<TextNode>(50);
    
    private BodyNode bodyNode = new BodyNode();
    private TagNode currentParent = bodyNode;
    
    private boolean documentStarted = false;
    private boolean documentEnded = false;
    
    public LeafComparator() {
        super();
    }
    
    public BodyNode getBody(){
        return bodyNode;
    }
    
    public void startDocument() throws SAXException {
        if(documentStarted)
            throw new IllegalStateException("This Handler only accepts one document");
        documentStarted=true;
    }

    public void endDocument () throws SAXException {
        if(!documentStarted || documentEnded)
            throw new IllegalStateException();
        endWord();
        documentEnded=true;
        documentStarted=false;
    }

    private boolean bodyStarted = false;
    private boolean bodyEnded = false;
    
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        
        if (!documentStarted || documentEnded)
            throw new IllegalStateException();
        
        if (bodyStarted && !bodyEnded) {
            TagNode newTagNode = new TagNode(currentParent, qName, attributes);;
            System.out.println("Started: " + newTagNode.getOpeningTag()
                    +" with parent "+currentParent.getOpeningTag());
            currentParent = newTagNode;
        }else if(bodyStarted){
            //Ignoring element after body tag closed
        }else if(qName.equalsIgnoreCase("body")){
            bodyStarted = true;
        }
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        
        if (!documentStarted || documentEnded)
            throw new IllegalStateException();

        if(qName.equalsIgnoreCase("body")){
            bodyEnded = true;
        }else if (bodyStarted && ! bodyEnded) {
            endWord();
            System.out.println("Ended: " + currentParent.getEndTag());
            currentParent = currentParent.getParent();
        }
    }
    
    private StringBuilder newWord = new StringBuilder();
    
    public void characters(char ch[], int start, int length)
            throws SAXException {
        
        if (!documentStarted || documentEnded)
            throw new IllegalStateException();
        
        for(int i=start; i<start+length;i++){
            char c = ch[i];
            if(isValidDelimiter(c)){
                endWord(); 
                TextNode textNode = new TextNode(currentParent, c+"");
                
                leafs.add(textNode);
                currentParent.addChild(textNode);
                System.out.println("adding delimiter: "+ textNode.getText());
                
            }else{
                newWord.append(c);
            }
            
        }
    }
    
    private void endWord(){
        if (newWord.length()>0) {
            System.out.println("adding word: " + newWord.toString());
            leafs.add(new TextNode(currentParent, newWord.toString()));
            newWord.setLength(0);
        }        
    }

    public int getRangeCount() {
       return leafs.size();
    }
    
    public TextNode getLeaf(int i){
       return leafs.get(i);
    }
    
    public void markAsNew(int begin, int end){
        for(int i=begin;i<end;i++){
            getLeaf(i).markAsNew();
        }
    }

    public boolean rangesEqual(int i1, IRangeComparator rangeComp, int i2) {
        LeafComparator comp;
        try {
            comp = (LeafComparator)rangeComp;
        } catch (RuntimeException e) {
           return false;
        }
        
        return getLeaf(i1).equals(comp.getLeaf(i2)); 
    }

    public boolean skipRangeComparison(int arg0, int arg1, IRangeComparator arg2) {
        return false;
    }

    public static boolean isValidDelimiter(char c ){
        switch (c) {
        // Basic Delimiters
        case '/':
        case '.':
        case '!':
        case ',':
        case ';':
        case '?':
        case ' ':
        case '=':
        case '\'':
        case '"':
        case '\t':
        case '\r':
        case '\n':
        // Extra Delimiters
        case '[':
        case ']':
        case '{':
        case '}':
        case '(':
        case ')':
        case '&':
        case '|':
        case '\\':
        case '-':
        case '_':
        case '+':
        case '*':
        case ':':
            return true;
        default:
            return false;
        }
    }

}