package com.mst.model;

import com.mst.util.Constants;

public class MapValue {
	public String value; // the value assigned to the Attribute. This is obtained from the text.
	public String qualifier; // qualifies the value stored in the map, e.g. Admin of Drug may have a qualifier of Current. This value is stored
							 // along with the Attribute in the lookup table.
	public String debug; // for values determined by a constructor, this value shows the contructor pair that generated the result
	public boolean negated;
	public String source;
	public String negSource; // source of negation: PP, NP, SUBJ, SUBJC, VB
	//public Map<String, String> metadata = new HashMap<>();
	
	public MapValue(String value, String source) {
		// used by processRegex in StructuredOutput
		this(value, null, null, false, source, null);
	}
	
	//public MapValue2(String value, boolean negated) {
	//	this(value, null, null, negated, null, null);
	//}
	
	//public MapValue2(String value, String qualifier, String source) {
	//	this(value, qualifier, null, false, source, null);
	//}
	
	public MapValue(String value, String qualifier, String debug, String source) {
		this(value, qualifier, debug, false, source, null);
	}
	
	public MapValue(String value, String qualifier, String debug, boolean negated, String source, String negSource) {
		if(Constants.AGE_REGEX.matcher(value).matches()) {
			String[] age = value.split("-");
	    	value = age[0];
		}
		
		this.value = value;
		this.qualifier = qualifier;
		this.debug = debug;
		this.negated = negated;
		this.source = source;
		this.negSource = negSource;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(value).append("\t").append(qualifier).append("\t").append(debug).append("\t").append(negated).append("\t").append(source).append("\t").append(negSource);
		return sb.toString();
	}
}