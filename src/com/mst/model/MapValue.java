package com.mst.model;

import com.mst.util.Constants;

public class MapValue {
	public String value; // the value assigned to the Attribute. This is obtained from the text.
	public String qualifier; // qualifies the value stored in the map, e.g. Admin of Drug may have a qualifier of Current. This value is stored
							 // along with the Attribute in the lookup table.
	public String debug; // for values determined by a constructor, this value shows the contructor pair that generated the result
	public boolean negated;
	
	public MapValue(String value) {
		this(value, null, null, false);
	}
	
	public MapValue(String value, boolean negated) {
		this(value, null, null, negated);
	}
	
	public MapValue(String value, String qualifier) {
		this(value, qualifier, null, false);
	}
	
	public MapValue(String value, String qualifier, String debug) {
		this(value, qualifier, debug, false);
	}
	
	public MapValue(String value, String qualifier, String debug, boolean negated) {
		if(value.matches(Constants.AGE_REGEX)) {
			String[] age = value.split("-");
	    	value = age[0];
		}
		
		this.value = value;
		this.qualifier = qualifier;
		this.debug = debug;
		this.negated = negated;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(value).append(", ").append(qualifier).append(", ").append(debug).append(", ").append(negated);
		return sb.toString();
	}
}