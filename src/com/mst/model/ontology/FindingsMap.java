package com.mst.model.ontology;

import java.util.HashMap;
import java.util.Map;

public class FindingsMap {
	private Map<String,String> values = new HashMap<String,String>();
	private Map<String,Integer> keyIndex = new HashMap<String,Integer>();
	
	public void put(String key, String attribute, String value) {
		Integer index = keyIndex.get(key);
		if(index == null)
			index = 0;
		
		keyIndex.put(key, ++index);
		
		values.put(key + "." + index + "." + attribute, value);
	}
	
	public Map<String,String> getValues() {
		return values;
	}
}
