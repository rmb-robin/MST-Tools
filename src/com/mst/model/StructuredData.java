package com.mst.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Multimap;

public class StructuredData {
	public String patientID;
	public Date date = new Date(0);
	public String practice;
	public String study;
	// related to a verb phrase
	public List<Multimap<String, MapValue>> related = new ArrayList<Multimap<String, MapValue>>();
	// unrelated to a verb phrase; i.e., appears in a noun or prep phrase that cannot be grammatically related (by current metadata) to a verb phrase
	//public Multimap<String, MapValue> unrelated = ArrayListMultimap.create();
	public List<Multimap<String, MapValue>> unrelated = new ArrayList<Multimap<String, MapValue>>();
	public List<Multimap<String, MapValue>> regex = new ArrayList<Multimap<String, MapValue>>();
	public String sentence;

	public List<MapValue> getValue(String searchKey, String searchValue, String targetKey) {	
		List<MapValue> list = new ArrayList<MapValue>();
		
		MapValue value = getValueFromList(regex, searchKey, searchValue, targetKey);
		
		if(value != null)
			list.add(value);
			
		value = getValueFromList(related, searchKey, searchValue, targetKey);
		
		if(value != null)
			list.add(value);
			
		value = getValueFromList(unrelated, searchKey, searchValue, targetKey);
		
		if(value != null)
			list.add(value);
		
		return list;
	}
	
	public MapValue getValueFromList(List<Multimap<String, MapValue>> list, String queryAttrName, String queryAttrValue, String sourceAttrName) {
		MapValue value = null;

		for(Multimap<String, MapValue> map : list) {
			Collection<MapValue> foo = map.get(queryAttrName); // ex. Diagnostic Procedure
			for(MapValue item : foo) {
				if(item.value.matches("(?i)"+queryAttrValue)) { // ex. Gleason
					Collection<MapValue> abs = map.get(sourceAttrName);
					MapValue[] vals = abs.toArray(new MapValue[0]);
					if(vals.length > 0)
						value = vals[0];
				}
			}	
		}
		
		return value;
	}
}
