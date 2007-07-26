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

import org.outerj.daisy.diff.lcs.rendered.dom.helper.LastCommonParentResult;

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
       // System.out.println("LastCommonParent of "+this+" in "+this.getParent()+" and "+other+" in "+other.getParent());
        while (isSame && i < myParents.size() && i < otherParents.size()) {
           // System.out.println("comparing " + myParents.get(i) + " to "
           //         + otherParents.get(i));
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
           // System.out.println("case 1: 2 tags did not match");
            // There were 2 tags that did not match
            result.setIndexInLastCommonParent(myParents.get(i - 1).getIndexOf(
                    myParents.get(i)));
            result.setSplittingNeeded();
        } else if (myParents.size() < otherParents.size()) {
           // System.out.println("case 2: all matched but more in old tree");
            // All tags matched but there are tags left in the other tree
            result.setIndexInLastCommonParent(myParents.get(i - 1).getIndexOf(
                    this));
        } else if (myParents.size() > otherParents.size()) {
          //  System.out.println("case 3: all matched but more in new tree");
            // All tags matched but there are tags left in this tree
            result.setIndexInLastCommonParent(myParents.get(i - 1).getIndexOf(
                    myParents.get(i)));
            result.setSplittingNeeded();
        } else {
//            System.out.println("case 4: everything matched or only BODY");
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
