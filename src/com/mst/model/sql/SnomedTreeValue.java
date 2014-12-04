package com.mst.model.sql;

public class SnomedTreeValue {
	public String id;
	public String value;
	public int tree_id;
	public int position;
	
	public SnomedTreeValue(String id, String value, int tree_id, int position) {
		this.id = id;
		this.value = value;
		this.tree_id = tree_id;
		this.position = position;
	}
}
