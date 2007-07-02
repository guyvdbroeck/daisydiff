/*
 * Copyright 2004 Outerthought bvba and Schaubroeck nv
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
package org.outerj.daisy.diff.lcs.rangecomparator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.rangedifferencer.IRangeComparator;
import org.outerj.daisy.diff.lcs.tag.Atom;
import org.outerj.daisy.diff.lcs.tag.DelimiterAtom;
import org.outerj.daisy.diff.lcs.tag.TagAtom;
import org.outerj.daisy.diff.lcs.tag.TextAtom;

public class TagComparator implements IRangeComparator {

	private List<Atom> atoms = new ArrayList<Atom>(50);
	
	public TagComparator(String s){
		generateAtoms(s);
	}
	
	public TagComparator(StringBuilder s){
		generateAtoms(s.toString());
	}
	
	public List<Atom> getAtoms(){
		return new ArrayList<Atom>(atoms);
	}
	
	private void generateAtoms(String s) {
		if(atoms.size()>0)
			throw new IllegalStateException("Atoms can only be generated once");
		
		StringBuilder currentWord = new StringBuilder(100);
		
		for(int i=0;i<s.length();i++){
			char c = s.charAt(i);
			
			if(c=='<' && TagAtom.isValidTag(s.substring(i, s.indexOf('>',i)+1))){	
				//a tag
				if(currentWord.length()>0){
					atoms.add(new TextAtom(currentWord.toString()));
					currentWord.setLength(0);
				}
				
				int end = s.indexOf('>',i);
				atoms.add(new TagAtom(s.substring(i, end+1)));
				i=end;
			}else if(DelimiterAtom.isValidDelimiter(c)){
				//a delimiter
				if(currentWord.length()>0){
					atoms.add(new TextAtom(currentWord.toString()));
					currentWord.setLength(0);
				}
				
				atoms.add(new DelimiterAtom(c));
			}else{
				//something else
				currentWord.append(c);
			}
		}
		if(currentWord.length()>0){
			atoms.add(new TextAtom(currentWord.toString()));
			currentWord.setLength(0);
		}
	}

	public String substring(int startAtom, int endAtom) {
//		System.out.println("start="+startAtom);
//		System.out.println("end="+endAtom);
        if (startAtom == endAtom) {
            return atoms.get(startAtom).getFullText();
        } else {
            StringBuilder result = new StringBuilder();
            for (int i = startAtom; i < endAtom; i++) {
                result.append(atoms.get(i).getFullText());
            }
            return result.toString();
        }
    }
	
	public String substring(int startAtom) {
		return substring(startAtom, atoms.size());
    }
	
	public Atom getAtom(int i){
		if(i<0  || i>=atoms.size())
			throw new IndexOutOfBoundsException();
		return atoms.get(i);
	}
	

	public int getRangeCount() {
		return atoms.size();
	}

	public boolean rangesEqual(int thisIndex, IRangeComparator other,
			int otherIndex) {
		TagComparator tc2;
		try {
			tc2 = (TagComparator)other;
		} catch (ClassCastException e) {
			return false;
		}
		return tc2.getAtom(otherIndex).equalsIdentifier(getAtom(thisIndex));
	}

	public boolean skipRangeComparison(int length, int maxLength,
			IRangeComparator other) {
		return false;
	}

}
