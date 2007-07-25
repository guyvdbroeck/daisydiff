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
package org.outerj.daisy.diff.lcs.rendered.dom;

import java.util.ArrayList;
import java.util.List;

import org.outerj.daisy.diff.lcs.rendered.acestor.AncestorComparator;

public class TextNode extends Node {

    private String s;

    public TextNode(TagNode parent, String s) {
        super(parent);

        this.s = s;
        normalizeNewlines();
    }

    //Change all newlines to spaces when this node is not inside a <pre> tag
    private void normalizeNewlines() {
        if(!(s.equalsIgnoreCase("\n")||s.equalsIgnoreCase("\r")))
            return;
        List<TagNode> ancestors = getParentTree();
        for(TagNode ancestor:ancestors){
            if(ancestor.getQName().equalsIgnoreCase("pre")){
                return;
            }
        }
        s=" ";
    }

    public String getText() {
        return s;
    }

    public boolean isSameText(Object other) {
        if (other == null)
            return false;

        TextNode otherTextNode;
        try {
            otherTextNode = (TextNode) other;
        } catch (ClassCastException e) {
            System.out.println("ClassCastException");
            return false;
        }
        return getText().replace('\n', ' ').equals(
                otherTextNode.getText().replace('\n', ' '));
    }

    private boolean isNew = false;

    public void markAsNew() {
        isNew = true;
    }

    public boolean isNew() {
        return isNew;
    }

    private boolean isChanged = false;

    public void compareTags(List<TagNode> other) {

        AncestorComparator acthis = new AncestorComparator(this.getParentTree());
        AncestorComparator acother = new AncestorComparator(other);

        isChanged = acthis.hasChanged(acother);

        if (isChanged) {
            changes = acthis.getCompareTxt();
        }
    }

    public boolean isChanged() {
        return isChanged;
    }

    private String changes = "";

    public String getChanges() {
        return changes;
    }

    public boolean isDeleted() {
        return deletedId>-1;
    }

    private int deletedId = -1;

    public void markAsDeleted(int start) {
        deletedId = start;
    }

    public boolean isDeleted(int id) {
        return this.deletedId == id;
    }

    @Override
    public List<Node> getMinimalDeletedSet(int start) {
        List<Node> nodes = new ArrayList<Node>(1);
        if (isDeleted(start))
            nodes.add(this);

        return nodes;
    }

    public String toString() {
        return getText();
    }


    private boolean isIgnorable;
    
    public void markAsIgnorable() {
        isIgnorable=true;
    }
    
    public boolean isIgnorable(){
        return isIgnorable;
    }

    public boolean isWhitespace(){
        if(s.length()!=1)
            return false;
        else
            switch(s.charAt(0)){
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                    return true;
                default:
                    return false;
            }
    }
    
}
