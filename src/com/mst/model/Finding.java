package com.mst.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mst.util.Constants;

public class Finding {
	public String type; // Admin of Drug or Diagnostic Procedure
	public Object value; // Lupron or PSA
	public Object subValue = null; // null or 2.5 (not yet in use. the idea was to have this be the value for something like a PSA test)
	public String source;
	public Set<String> negSource = new HashSet<>();
	public String verb;
	public String verbTense;
	public String debug;
	public List<Finding> children = new ArrayList<Finding>();
	
	public Finding() { }
	
	public Finding(String type, Object value, String source, Set<String> negSource, String verb, String verbTense, String debug) { 
		this.type = type;
		if(debug != null && debug.equalsIgnoreCase("number") && Constants.NUMERIC.matcher(String.valueOf(value)).matches()) {
			if(String.valueOf(value).indexOf('.') > -1)
				this.value = Float.valueOf((String) value);
			else
				this.value = Integer.valueOf((String) value);
		} else {
			this.value = value;
		}
		this.source = source;
		//this.negSource = negSource; // results in clearing of Set when parent Set is cleared
		this.verb = verb;
		this.verbTense = verbTense;
		this.debug = debug;
		if(negSource != null)
			for(String s : negSource)
				this.negSource.add(s);
	}
	
	public String getNotationString(Constants.StructuredNotationReturnValue returnValue) {
		String temp = "";
		
		String rv = getReturnValueString(this, returnValue);
		
		if(returnValue.toString().endsWith("SOLO")) {
			temp += normalizeValue(rv);
		} else 
			temp += normalizeType(this.type) + ":" + normalizeValue(rv);
			
		temp += processChildren("", this, returnValue);
		
		if(returnValue == Constants.StructuredNotationReturnValue.NONE) {
			temp = temp.replace(":", "");
		}
		
		return temp;
	}
	
//	public Map<String, String> flatten() {
//		Map<String, String> flat = new HashMap<>();
//		 
//		flat.put(this.type, String.valueOf(this.value));
//		flat.put("verb", this.verb);
//		flat.put("verbTense", this.verbTense);
//		
//		generateMap(flat, this);
//		
//		return flat;
//	}
	
	public Multimap<String, Object> flatten() {
		Multimap<String, Object> flat = ArrayListMultimap.create(); 
		flat.put(this.type, String.valueOf(this.value).toLowerCase());
		if(this.verb != null)
			flat.put("verb", this.verb);
		if(this.verbTense != null)
			flat.put("verbTense", this.verbTense);
		flat.put("negated", this.negSource.isEmpty() ? "N" : "Y");
		
		generateMap(flat, this);
		
		return flat;
	}
	
//	private void generateMap(Map<String, String> map, Finding finding) {
//		for(int i=0; i < finding.children.size(); i++) {
//			Finding child = finding.children.get(i);
//			map.put(child.type, String.valueOf(child.value));
//			map.put("verb", child.verb);
//			map.put("verbTense", child.verbTense);
//			
//			generateMap(map, child);
//		}
//	}
	
	private void generateMap(Multimap<String, Object> map, Finding finding) {
		for(int i=0; i < finding.children.size(); i++) {
			Finding child = finding.children.get(i);
			//map.put(child.type, String.valueOf(child.value));
			map.put(child.type, child.value);
			
			generateMap(map, child);
		}
	}
	
	private String processChildren(String temp, Finding finding, Constants.StructuredNotationReturnValue returnValue) {
		for(int i=0; i < finding.children.size(); i++) {
			if(i==0)
				temp += "[";
			Finding child = finding.children.get(i);
			
			// do, or do not, append the Finding Type
			if(returnValue.toString().endsWith("SOLO")) {
				temp += normalizeValue(getReturnValueString(child, returnValue));
			} else
				temp += normalizeType(child.type) + ":" + normalizeValue(getReturnValueString(child, returnValue));
			
			temp = processChildren(temp, child, returnValue) + (i < finding.children.size()-1 ? "," : "]");
		}
		//System.out.println(temp);
		return temp;
	}
	
	private String getReturnValueString(Finding finding, Constants.StructuredNotationReturnValue returnValue) {
		String rv = "";
		
		switch(returnValue) {
			case SOURCE:
			case SOURCE_SOLO:
				rv = finding.source; // CF: vb-obj-np [FS: vb-obj-np]
				break;
			case VALUE:
				rv = String.valueOf(finding.value); // CF: cyst [FS: liver]
				break;
			case ST:
				rv = finding.debug; // CF: neop [FS: bpoc]
				break;
			case NONE:
				rv = ""; // CF: [FS: ]
				break;
			default:
				rv = String.valueOf(finding.value);
		}
		return rv;
	}
	
	private String normalizeType(String type) {
		String val = "";
		for(String word : type.split(" ")) {
			val += word.substring(0,1);
		}
		return val.toUpperCase();
	}
	
	private String normalizeValue(Object value) {
		if(value != null)
			return value.toString().toLowerCase();
		else
			return "";
	}
}
