package com.mst.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Multimap;

public class StructuredData {
	public String patientId;
	public Date date = new Date(0);
	public String practice;
	public String study;
	public List<Multimap<String, MapValue>> data = new ArrayList<Multimap<String, MapValue>>();
	public String sentence;

	public List<MapValue> getValue(String searchKey, String searchValue, String targetKey) {	
		return getValue(searchKey, searchValue, targetKey, ".*", ".*");
	}
	
	public List<MapValue> getValue(String searchKey, String searchValue, String targetKey, String targetValue, String source) {	
		List<MapValue> list = new ArrayList<MapValue>();
		
		getValueFromList(data, searchKey, searchValue, targetKey, targetValue, list, source);
		
		return list;
	}
	
	public void getValueFromList(List<Multimap<String, MapValue>> list, String searchKey, String searchValue, String targetKey, String targetValue, List<MapValue> outputList, String source) {

		for(Multimap<String, MapValue> map : list) {
			for(MapValue mapValue : map.get(searchKey)) { // ex. Diagnostic Procedure
				if(mapValue.value.matches("(?i)"+searchValue) && // ex. Gleason
				   mapValue.source.matches("(?i)"+source)) { // ex. related
					if(targetKey.equalsIgnoreCase(searchKey)) {
						outputList.add(mapValue);
					} else {
						// TODO this needs to get the map for the sourceAttrName, not the current map. Ex. Clinical Finding|cancer|Finding Site
						// TODO what will this change break?
						for(MapValue valItem : map.get(targetKey)) {
							if(valItem.value.matches("(?i)"+targetValue)) {
								outputList.add(valItem);
							}
						}
					}
				}
			}
		}
	}
	
	// this definitely has issues. doesn't take into account multiple dates in a structured json list ("PSA on 3/22/2013 was 8.6 and 7.3 on 7/1/2013.").
	public String getKnownEventDate(String queryAttrName, String queryAttrValue) {
		List<MapValue> result = getValue(queryAttrName, queryAttrValue, "Known Event Date");
		if(result.isEmpty())
			return "";
		else
			return result.get(0).value;
	}
}
