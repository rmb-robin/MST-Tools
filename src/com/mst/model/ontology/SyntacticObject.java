package com.mst.model.ontology;

import java.util.ArrayList;
import java.util.Date;

public class SyntacticObject {
	public String _id;
	public String value;
	public String part_of_speech;
	public String semantic_type;
	public String snomed_id;
	public ArrayList<SnomedTreeValue> snomed_tree;
	public String infinitive;
	public ArrayList<String> source = new ArrayList<String>();
	public ArrayList<String> _class = new ArrayList<String>();
	public Date last_modified;
	public String modified_by;
	
	public class SnomedTreeValue {
		public int tree_id;
		public int position;
		public String id;
		public String value;
		
		public SnomedTreeValue(int tree_id, int position, String id, String value) {
			this.tree_id = tree_id;
			this.position = position;
			this.id = id;
			this.value = value;
		}
	}
}
