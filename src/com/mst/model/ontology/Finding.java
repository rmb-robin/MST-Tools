package com.mst.model.ontology;

import java.util.ArrayList;

public class Finding {
	public enum FindingType { PRESENT, ABSENT, SUSPICIOUS, UNKNOWN }
	public FindingType type;
	public String value = "";
	public String size = "";
	public boolean clinical;
	public ArrayList<FindingSite> findingSites = new ArrayList<FindingSite>();
	
	public Finding() { }
	
	public Finding(String value) {
		this.value = value;
		//findingSites.add(new FindingSite(value));
	}
}
