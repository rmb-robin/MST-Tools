package com.mst.model.ontology;

import java.util.ArrayList;

public class SemanticObject {
	public String concept;
	public String domain;
	public String category;
	public ArrayList<Rule> rules;
	
	public class Rule {
		public String target;
		public String type;
		//public ArrayList<SyntacticObject> values;
		public ArrayList<String> values;
		public int position;
	}
}
