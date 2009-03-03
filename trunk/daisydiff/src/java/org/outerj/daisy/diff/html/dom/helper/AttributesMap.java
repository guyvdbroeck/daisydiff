package org.outerj.daisy.diff.html.dom.helper;

import java.util.HashMap;

import org.xml.sax.Attributes;
/**
 * Map is used to store DOM tag attribute names and values. This map pays no attention to sequence of attributes.
 * @author karol
 *
 */
public class AttributesMap extends HashMap<String, String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6165499554111988049L;

	public AttributesMap() {
		super();
	}

	public AttributesMap(Attributes attributes) {
		super();
		for (int i = 0; i < attributes.getLength(); i++) {
			put(attributes.getQName(i).toLowerCase(), attributes.getValue(i));
		}
	}

	/**
	 * this method returns, if two maps have the same set of keys and values assigned to these keys.
	 */
	@Override
	public boolean equals(Object obj) {
		boolean equals = false;
		if (obj instanceof AttributesMap) {
			AttributesMap attributesMap = (AttributesMap) obj;
			
			if(size() == attributesMap.size()){
				equals = true;
				
				for(String attrib:keySet()){
					String localValue = get(attrib);
					String externalValue = attributesMap.get(attrib);
					
					if(externalValue == null || !externalValue.equals(localValue)){
						equals = false;
						break;
					}
				}
			}
		}
		return equals;
	}
}
