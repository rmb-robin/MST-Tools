package com.mst.util;

public class CacheKeyGenerator {

	public static String getKey(String type,String key){
		return key + "-" + type; 
	}
}
