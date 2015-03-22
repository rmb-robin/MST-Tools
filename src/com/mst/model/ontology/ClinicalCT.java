package com.mst.model.ontology;

import java.util.ArrayList;

public class ClinicalCT {
//	public String patientID = "";
//	public String sentenceID = "";
//	public String fullSentence = ""; 
//	public ArrayList<EvaluationFinding> findings = new ArrayList<EvaluationFinding>();
//	
//	public ArrayList<String> bpocList = new ArrayList<String>();
//	public ArrayList<String> spcoList = new ArrayList<String>();
//	public ArrayList<String> dsynList = new ArrayList<String>();
//	
//	public boolean addFindingSite(String findingSite) {
//		boolean ret = false;
//
//		EvaluationFinding cf = new EvaluationFinding();
//		FindingSite fs = new FindingSite(findingSite);
//		cf.findingSites.add(fs);
//		this.findings.add(cf);
//		
//		return ret;
//	}
//	
//	public boolean addFinding(String finding) {
//		boolean ret = false;
//		// locate clinical finding that doesn't already have a value
//		for(EvaluationFinding cf : this.findings) { 
//			if(cf.value.isEmpty()) {
//				cf.value = finding;
//				ret = true;
//				break;
//			}
//		}
//		return ret;
//	}
//	
//	public boolean addLaterality(String laterality) {
//		boolean ret = false;
//		// locate finding site that doesn't already have a laterality value
//		for(EvaluationFinding cf : this.findings) {
//			for(FindingSite fs : cf.findingSites) { 
//				if(fs.laterality.isEmpty()) {
//					fs.laterality = laterality;
//					ret = true;
//					break;
//				}
//			}
//			if(ret)
//				break;
//		}
//		return ret;
//	}
//	
//	public boolean addLocation(String location) {
//		boolean ret = false;
//		// locate finding site that doesn't already have a location value
//		for(EvaluationFinding cf : this.findings) {
//			for(FindingSite fs : cf.findingSites) { 
//				if(fs.location.isEmpty()) {
//					fs.location = location;
//					ret = true;
//					break;
//				}
//			}
//			if(ret)
//				break;
//		}
//		return ret;
//	}
}
