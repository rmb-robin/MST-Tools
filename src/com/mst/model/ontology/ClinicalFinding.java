package com.mst.model.ontology;

import java.util.ArrayList;

public class ClinicalFinding {

	public String value;  //cyst, mass, tumor
	public String _class;  //Appearance-Viz
	//public String type; //Presence, Absence
	//public String parent;
	public ArrayList<FindingSite> findingSites = new ArrayList<FindingSite>();
	//public String match; //string representing query that matched from constructors table
	public String complainingOfPain;
	public String diagnosis;
	public String diagnosis_failover;
	public String diagnosis_val;
	public String gleason;
	public String psa;
	public String psa_date;

}
