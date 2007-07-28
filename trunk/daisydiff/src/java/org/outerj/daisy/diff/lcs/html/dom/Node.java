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
package org.outerj.daisy.diff.lcs.html.dom;

import java.util.ArrayList;
import java.util.List;

import org.outerj.daisy.diff.lcs.html.dom.helper.LastCommonParentResult;
/**
 * Represents any element in the DOM tree of a HTML file.
 */
public abstract class Node {

    protected TagNode parent;

    public Node(TagNode parent) {
        this.parent = parent;
        if (parent != null)
            parent.addChild(this);
    }

    public TagNode getParent() {
        return parent;
    }

    public List<TagNode> getParentTree() {
        List<TagNode> parentTree = new ArrayList<TagNode>(5);
        if (getParent() != null) {
            parentTree.addAll(getParent().getParentTree());
            parentTree.add(getParent());
        }
        return parentTree;
    }

    public abstract List<Node> getMinimalDeletedSet(int start);

    public void detectIgnorableWhiteSpace(){
        //no op
    }
    
    public LastCommonParentResult getLastCommonParent(Node other) {
        if (other == null)
            throw new IllegalArgumentException("The given TextNode is null");

        LastCommonParentResult result = new LastCommonParentResult();

        List<TagNode> myParents = getParentTree();
        List<TagNode> otherParents = other.getParentTree();

        int i = 1;
        boolean isSame = true;
        while (isSame && i < myParents.size() && i < otherParents.size()) {
            if (!myParents.get(i).isSameTag(otherParents.get(i))) {
                isSame = false;
            } else {
                // After the while, the index i-1 must be the last common parent
                i++;
            }
        }

        result.setLastCommonParentDepth(i - 1);
        result.setLastCommonParent(myParents.get(i - 1));
        
        if (!isSame) {
            result.setIndexInLastCommonParent(myParents.get(i - 1).getIndexOf(
                    myParents.get(i)));
            result.setSplittingNeeded();
        } else if (myParents.size() < otherParents.size()) {
            result.setIndexInLastCommonParent(myParents.get(i - 1).getIndexOf(
                    this));
        } else if (myParents.size() > otherParents.size()) {
            // All tags matched but there are tags left in this tree
            result.setIndexInLastCommonParent(myParents.get(i - 1).getIndexOf(
                    myParents.get(i)));
            result.setSplittingNeeded();
        } else {
            // All tags matched untill the very last one in both trees
            // or there were no tags besides the BODY
            result.setIndexInLastCommonParent(myParents.get(i - 1).getIndexOf(
                    this));
        }
        return result;
    }

    public void setParent(TagNode parent) {
        this.parent = parent;
    }

    public abstract Node copyTree();

}
