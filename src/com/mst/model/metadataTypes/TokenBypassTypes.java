package com.mst.model.metadataTypes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TokenBypassTypes {

	public static HashSet<String> values = getValues();
	
	private static HashSet<String> getValues(){
		HashSet<String> result = new HashSet<String>();
		for(String token: getTokens()){
			String computedToken = token;
			for(int i =1;i<=10;i++){
				result.add(computedToken);
				computedToken += token;
			}
		}
		return result;
	}
	
    private static List<String> getTokens(){
		List<String> result = new ArrayList<>();
		result.add(")");
		result.add("(");
//		result.add("-");
		result.add(".");
		result.add("*");
		result.add("+");
//		result.add(",");
		result.add("?");
		result.add("/");
		result.add("'\'");
	    result.add("~");
	    result.add("@");
	    result.add("&");
	    result.add("#");
	    result.add("%");
	    result.add(":");
		return result;
		
	}
	
	
	
	
	
	
}
