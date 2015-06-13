package com.mst.model;

public class Constructor {
	
	public String key1;
	public String key2;
	public String attributeName;
	public int sourceIdx;
	
	public Constructor(String attributeName, int sourceIdx) {
		this.attributeName = attributeName;
		this.sourceIdx = sourceIdx;
	}
}
