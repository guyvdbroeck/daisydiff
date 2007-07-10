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

    public TagNode getLastCommonParent(Node other) {

        splittingNeeded = false;

        if (other == null)
            throw new IllegalArgumentException("The given TextNode is null");

        List<TagNode> myParents = getParentTree();
        List<TagNode> otherParents = other.getParentTree();

        int lastIndex = 0;

        
        
        for (int i = 1; i < myParents.size() && i < otherParents.size(); i++) {
            System.out.println("comparing "+myParents.get(i)+" to "+otherParents.get(i));
            if (myParents.get(i).isSameTag(otherParents.get(i))) {
                lastIndex = i;
            } else {
                lastCommonParentIndex = myParents.get(lastIndex).getIndexOf(
                        myParents.get(i));
                lastCommonParentDepth = lastIndex;
                splittingNeeded = true;
                return myParents.get(lastIndex);
            }
        }

        // There were no parents besides the BODY
        if (myParents.size() <= 1) {
            lastCommonParentIndex = myParents.get(0).getIndexOf(this);
            lastCommonParentDepth = 0;
        }// All tags matched
        else if (myParents.size() <= otherParents.size()) {
            lastCommonParentIndex = myParents.get(lastIndex).getIndexOf(this);
            lastCommonParentDepth = lastIndex;
        } else {
            lastCommonParentIndex = myParents.get(lastIndex).getIndexOf(
                    myParents.get(lastIndex + 1));
            lastCommonParentDepth = lastIndex;
            splittingNeeded = true;
        }
        return myParents.get(lastIndex);
    }

    private boolean splittingNeeded = false;

    public boolean isSplittingNeeded() {
        return splittingNeeded;
    }

    private int lastCommonParentDepth = -1;

    public int getLastCommonParentDepth() {
        return lastCommonParentDepth;
    }

    private int lastCommonParentIndex = -1;

    public int getLastCommonParentIndex() {
        return lastCommonParentIndex;
    }

    public void setParent(TagNode parent) {
        this.parent = parent;
    }

}
