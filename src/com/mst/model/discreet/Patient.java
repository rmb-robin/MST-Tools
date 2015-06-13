package com.mst.model.discreet;

import java.util.ArrayList;
import java.util.List;

public class Patient {
	public String patientId;
	public String practice; // practice ID
	public String sourceFile;
	public List<Meds> meds = new ArrayList<Meds>();
	public List<Discreet> discreet = new ArrayList<Discreet>();
	
	public Patient(String patientId) {
		this.patientId = patientId;
	}
	
	public Patient(String patientId, String practice, String sourceFile) {
		this.patientId = patientId;
		this.practice = practice;
		this.sourceFile = sourceFile;
	}
}
