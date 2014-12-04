package com.mst.model.ontology;

import java.util.ArrayList;

public class ClinicalCTOld {
	public String patientID = "";
	public String sentenceID = "";
	public String fullSentence = ""; 
	//public ArrayList<ClinicalFinding> clinicalFindings = new ArrayList<ClinicalFinding>();
	public ArrayList<FindingSiteOld> findingSites = new ArrayList<FindingSiteOld>();
	
	public ArrayList<String> bpocList = new ArrayList<String>();
	public ArrayList<String> spcoList = new ArrayList<String>();
	public ArrayList<String> dsynList = new ArrayList<String>();
	
	public FindingSiteOld getFindingSite(String findingSite) {
		FindingSiteOld ret = null;
		// locate finding site that doesn't already have a clinical finding
		for(FindingSiteOld fs : this.findingSites) { 
//			if(fs.value.equalsIgnoreCase(findingSite) && fs.clinicalFindings.isEmpty()) {
//				ret = fs;
//				break;
//			}
			if(fs.clinicalFindings.isEmpty()) {
				ret = fs;
				break;
			}
		}
		return ret;
	}
	
	public boolean addLaterality(String laterality, String findingSite) {
		boolean ret = false;
		// TODO is checking finding site overkill?
		// locate finding site that doesn't already have a laterality value
		for(FindingSiteOld fs : this.findingSites) { 
			if(fs.value.equalsIgnoreCase(findingSite) && fs.laterality.isEmpty()) {
				fs.laterality = laterality;
				ret = true;
				break;
			}
		}
		return ret;
	}
	
	public boolean addLocation(String location) {
		boolean ret = false;
		// locate finding site that doesn't already have a location value
		for(FindingSiteOld fs : this.findingSites) { 
			if(fs.location.isEmpty()) {
				fs.location = location;
				ret = true;
				break;
			}
		}
		return ret;
	}
}
