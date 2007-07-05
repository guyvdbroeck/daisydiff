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
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.compare.rangedifferencer.IRangeComparator;
import org.outerj.daisy.diff.lcs.tag.Atom;
import org.outerj.daisy.diff.lcs.tag.DelimiterAtom;
import org.outerj.daisy.diff.lcs.tag.IAtomSplitter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DiffContentHandler extends DefaultHandler implements IAtomSplitter
{

    private List<MarkupTextAtom> atoms = new ArrayList<MarkupTextAtom>(50);
    private Deque<Tag> tags = new LinkedList<Tag>();
    
    private boolean documentStarted = false;
    private boolean documentEnded = false;
    
    
    
    public DiffContentHandler ()
    {
	super();
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

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        
        if (!documentStarted || documentEnded)
            throw new IllegalStateException();
        
        Tag newTag = new Tag(qName, attributes);
        
        tags.push(newTag);
        
        System.out.println("started: " + newTag);
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        
        if (!documentStarted || documentEnded)
            throw new IllegalStateException();
        
        endWord();
        Tag poped=tags.pop();
        System.out.println("Ended: " + poped.toBasicString());;
    }
    
    private StringBuilder newWord = new StringBuilder();
    
    public void characters(char ch[], int start, int length)
            throws SAXException {
        
        if (!documentStarted || documentEnded)
            throw new IllegalStateException();
        
        for(int i=start; i<start+length;i++){
            char c = ch[i];
            if(DelimiterAtom.isValidDelimiter(c)){
                endWord(); 
                atoms.add(new MarkupTextAtom(newWord.toString()
                        , tags.toArray(new Tag[tags.size()])));
                
            }else{
                newWord.append(c);
            }
            
        }
    }
    
    private void endWord(){
        System.out.println("adding word: "+newWord.toString());
        atoms.add(new MarkupTextAtom(newWord.toString()
                , tags.toArray(new Tag[tags.size()])));
        newWord.setLength(0);
    }

    public Atom getAtom(int i) {
        if (i < 0 || i >= atoms.size())
            throw new IndexOutOfBoundsException("There is no Atom with index "+i);
	return atoms.get(i);
    }

    public String substring(int startAtom, int endAtom) {
	// TODO Auto-generated method stub
	return null;
    }

    public String substring(int startAtom) {
	// TODO Auto-generated method stub
	return null;
    }

    public int getRangeCount() {
	// TODO Auto-generated method stub
	return 0;
    }

    public boolean rangesEqual(int arg0, IRangeComparator arg1, int arg2) {
	// TODO Auto-generated method stub
	return false;
    }

    public boolean skipRangeComparison(int arg0, int arg1, IRangeComparator arg2) {
	// TODO Auto-generated method stub
	return false;
    }
    
    

}