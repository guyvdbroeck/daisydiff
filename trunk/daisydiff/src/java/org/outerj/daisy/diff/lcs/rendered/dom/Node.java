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
    
    public Node(TagNode parent){
        this.parent = parent;
        if(parent != null)
            parent.addChild(this);
    }

    public TagNode getParent() {
        return parent;
    }
    
    public List<TagNode> getParentTree() {
        List<TagNode> parentTree = new ArrayList(5);
        if(getParent()!=null){
            parentTree.addAll(getParent().getParentTree());
            parentTree.add(getParent());
        }
        return parentTree;
    }
    
    public abstract List<Node>  getMinimalDeletedSet(int start);

    public TagNode getLastCommonParent(Node other) {
        if(other == null)
            throw new IllegalArgumentException("The given TextNode is null");
        
        List<TagNode> myParents = getParentTree();
        List<TagNode> otherParents = other.getParentTree();
        
        int lastIndex=0;
        
        for(int i=1;i<myParents.size() && i<otherParents.size();i++){
            if(myParents.get(i).isSameTag(otherParents.get(i))){
                lastIndex=i;
            }else{
                afterLastCommonParentIndex = myParents.get(lastIndex).getIndexOf(
                                        myParents.get(i))+1;
                return myParents.get(lastIndex);
            }
        }
        
        //There were no parents besides the BODY
        afterLastCommonParentIndex=myParents.get(0).getIndexOf(other)+1;
        
        return myParents.get(lastIndex);
    }
    
    private int afterLastCommonParentIndex=-1;
    
    public int getAfterLastCommonParentIndex(){
        return afterLastCommonParentIndex;
    }
    public void setParent(TagNode parent) {
        this.parent = parent;
    }

}
