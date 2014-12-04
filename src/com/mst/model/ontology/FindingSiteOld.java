package com.mst.model.ontology;

import java.util.ArrayList;

public class FindingSiteOld {
	public String value = "";
	public ArrayList<ClinicalFinding> clinicalFindings = new ArrayList<ClinicalFinding>();
	public String laterality = "";
	public String location = "";

	public FindingSiteOld() { }
	
	public FindingSiteOld(String value) {
		this.value = value;
	}
	
}
