package com.mst.model.ontology;

import java.util.Date;
import java.util.ArrayList;

public class Findings {

	public String fullSentence;
	public String patientID;
	public Date date = new Date(0);
	public 	ArrayList<Finding> findings = new ArrayList<Finding>();
	
	public Findings() { }
}
