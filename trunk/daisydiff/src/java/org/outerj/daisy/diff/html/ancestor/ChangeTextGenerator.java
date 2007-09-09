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
package org.outerj.daisy.diff.html.ancestor;

import java.util.Locale;

import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.outerj.daisy.diff.html.ancestor.tagtostring.TagToStringFactory;
import org.outerj.daisy.diff.html.dom.TagNode;

public class ChangeTextGenerator {

	private AncestorComparator ancestorComparator;
	private AncestorComparator other;
    private TagToStringFactory factory;
	private Locale locale; 
	
	public ChangeTextGenerator(AncestorComparator ancestorComparator,AncestorComparator other, Locale locale) {
		this.ancestorComparator=ancestorComparator;
		this.other=other;
		this.factory=new TagToStringFactory();
		this.locale = locale;
	}

	public ChangeText getChanged(RangeDifference...differences){
		ChangeText txt = new ChangeText(55);
		
		boolean rootlistopened=false;
		
		if(differences.length>1){
        	txt.addHtml("<ul class='changelist'>");
        	rootlistopened=true;
		}
		
        for (int j=0; j<differences.length;j++) {
        	
        	RangeDifference d = differences[j];
        	
        	boolean lvl1listopened=false;
        	
        	if(rootlistopened){
        		txt.addHtml("<li>");
        	}
        	
        	if(d.leftLength()+d.rightLength()>1){
        		txt.addHtml("<ul class='changelist'>");
        		lvl1listopened=true;
        	}
        	
        	//left are the old ones
        	for (int i = d.leftStart(); i < d.leftEnd(); i++) {
                if(lvl1listopened)
                	txt.addHtml("<li>");
                
            	//add a bullet for a old tag
            	addTagOld(txt, other.getAncestor(i));

                if(lvl1listopened)
                	txt.addHtml("</li>");
                
            }

        	//right are the new ones
        	for (int i = d.rightStart(); i < d.rightEnd(); i++) {
                if(lvl1listopened)
                	txt.addHtml("<li>");
                
                //add a bullet for a new tag
                	addTagNew(txt, this.getAncestor(i));
                	
                if(lvl1listopened)
                	txt.addHtml("</li>");
                
            }
        	
        	if(lvl1listopened){
        		txt.addHtml("</ul>");
        	}
        	
        	if(rootlistopened){
        		txt.addHtml("</li>");
        	}
        }
		
        if(rootlistopened){
        	txt.addHtml("</ul>");
        	rootlistopened=false;
		}
		
        return txt;

		
	}

	private void addTagOld(ChangeText txt, TagNode ancestor) {
		factory.create(ancestor, locale).getRemovedDescription(txt);
	}

	private void addTagNew(ChangeText txt, TagNode ancestor) {
		factory.create(ancestor, locale).getAddedDescription(txt);
	}

	private TagNode getAncestor(int i) {
		return ancestorComparator.getAncestor(i);
	}
	
}
