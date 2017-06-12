package com.mst.model.SentenceQuery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class EdgeQuery {

	private String name; 
	private HashSet<String> values ;
	private HashSet<String> lowerValues = null;
	private Boolean isNumeric = null;
	
	public EdgeQuery(){
		values = new HashSet<>();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public HashSet<String> getValues() {
	 return this.values;
	}
	
	public HashSet<String> getValuesLower(){
		if(lowerValues==null){
			lowerValues = new HashSet<String>();
			this.values.forEach(item->{
				lowerValues.add(item.toLowerCase());
				}
			);
		}
		return lowerValues;
	}
	
	public void setValues(HashSet<String> values) {
		this.values = values;
	}

	public Boolean getIsNumeric() {
		return isNumeric;
	}

	public void setIsNumeric(Boolean isNumeric) {
		this.isNumeric = isNumeric;
	} 
	
	
}
