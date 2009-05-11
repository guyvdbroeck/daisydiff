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
package org.outerj.daisy.diff.html.modification;

public enum ModificationType {

    CHANGED {
        @Override
        public String toString() {
            return "changed";
        }
    },

    REMOVED {
        @Override
        public String toString() {
            return "removed";
        }
    },

    ADDED {
        @Override
        public String toString() {
            return "added";
        }
    },

    /**
     * for <code>TextNode</code>s that were removed as
     * part of the substituted column
     */
    COLUMN_SUBSTITUTION_REMOVED {
    	@Override
    	public String toString(){
    		return "substituted as part of a column";
    	}
    },
    
    /**
     * for <code>TextNode</code>s that replaced removed ones
     * during the column substitution
     */
    COLUMN_SUBSTITUTION_ADDED {
    	@Override
    	public String toString(){
    		return "added as part of a column substitution";
    	}
    },
    
    COLUMN_REMOVED {
    	@Override
    	public String toString(){
    		return "removed as a part of a column";
    	}
    },
    
    COLUMN_ADDED {
    	@Override
    	public String toString(){
    		return "added as a part of a column";
    	}
    },
    
    ROW_SUBSTITUTION_REMOVED {
    	@Override
    	public String toString(){
    		return "substituted as part of a row";
    	}
    }, 
    
    ROW_SUBSTITUTION_ADDED {
    	@Override
    	public String toString(){
    		return "added as part of a row substitution";
    	}
    },
    
    ROW_MERGED {
    	@Override
    	public String toString(){
    		return "merged row";
    	}
    },
    
    ROW_SPLIT {
    	@Override
    	public String toString(){
    		return "split row";
    	}
    },
    
    ROW_REMOVED {
    	@Override
    	public String toString(){
    		return "removed as a part of a row";
    	}
    },
    
    ROW_ADDED {
    	@Override
    	public String toString(){
    		return "added as a part of a row";
    	}
    },
    
    NONE {
        @Override
        public String toString() {
            return "none";
        }
    };

}
