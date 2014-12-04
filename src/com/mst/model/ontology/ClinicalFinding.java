package com.mst.model.ontology;

//import java.util.ArrayList;

public class ClinicalFinding {
	public enum FindingType { PRESENT, ABSENT, SUSPICIOUS, UNKNOWN }
	public FindingType findingType;
	public String value;
	public String size;
	//public ArrayList<FindingSite> findingSites = new ArrayList<FindingSite>();

	public ClinicalFinding() { }
	
	public ClinicalFinding(String value) {
		this.value = value;
		//findingSites.add(new FindingSite(value));
	}
}
