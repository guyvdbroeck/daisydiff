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
package org.outerj.daisy.diff.lcs.tag;

import org.outerj.daisy.diff.lcs.rangecomparator.Atom;

public class DelimiterAtom extends TextAtom {

	public DelimiterAtom(char c){
		super(""+c);
	}
	
	public static boolean isValidDelimiter(String s){
		if(s.length()==1){
	        switch (s.charAt(0)) {
	        		//Basic Delimiters
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
	            	//Extra Delimiters
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
		return false;
	}
        
    public boolean isValidAtom(String s){
       	return super.isValidAtom(s) && isValidDelimiterAtom(s);
    }

	private boolean isValidDelimiterAtom(String s) {
		return isValidDelimiter(s);
	}
	
	public String toString(){
		return "DelimiterAtom: " + getFullText().replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r").replaceAll("\t", "\\\\t");
	}
	
	public boolean  equalsIdentifier(Atom a){
		return super.equalsIdentifier(a) 
			|| ((a.getIdentifier().equals(" ") || a.getIdentifier().equals("\n")) 
					&& (this.getIdentifier().equals(" ") || this.getIdentifier().equals("\n")));
	}
	
	
}
