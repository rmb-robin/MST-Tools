package com.mst.metadataProviders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaseProvider {

	protected String getString(String val){
		if(val==null) return null; 
		if(val.equals("")) return null;
		return val;
	}
	
	protected boolean convertToBool(String value){
		value = value.trim();
		if(value.toLowerCase().equals("t")) return true;
		if(value.toLowerCase().equals("f")) return false;
		return false;
	}
	
	protected List<String> getValuesforSemiColenList(String values){
		if(values==null || values.equals("")) return new ArrayList<String>();
		String[] splits = values.split(";");
		return Arrays.asList(splits);
	}
}
