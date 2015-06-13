package com.mst.model.ontology;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class Findings {

	public String patientID;
	public Date date = new Date(0);
	public List<String> sentences = new ArrayList<String>();
	public List<ClinicalFinding> clinicalFindings = new ArrayList<ClinicalFinding>();
	public List<ObservableEntity> observableEntities = new ArrayList<ObservableEntity>();
	//public PatientHistory patientHistory = new PatientHistory();
	public TherapeuticProcedure therapeuticProcedure = new TherapeuticProcedure();
	public SurgeryPlaceholder surgeryPlaceholder = new SurgeryPlaceholder();
	public SituationWithExplicitContext situationWithExplicitContext = new SituationWithExplicitContext();

	public Findings() {	}
	
	public Findings(String patientID) {
		this.patientID = patientID;
	}
}
