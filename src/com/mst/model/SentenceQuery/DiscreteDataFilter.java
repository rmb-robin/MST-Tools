package com.mst.model.SentenceQuery;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DiscreteDataFilter {
	private List<String> resultStatus;
	private List<String> readingLocation;
	private List<String> patientSex;
	private List<String> examDescription;
	private List<String> modality;
	private List<Integer> patientAge;
	private List<LocalDate> reportFinalizedDate;
	

	public DiscreteDataFilter(){
		resultStatus = new ArrayList<>();
		readingLocation = new ArrayList<>();
		patientSex = new ArrayList<>();
		examDescription = new ArrayList<>();
		modality = new ArrayList<>();
		patientAge = new ArrayList<>();
		reportFinalizedDate = new ArrayList<>();
	}
	

	public List<Integer> getPatientAge() {
		return patientAge;
	}
	public void setPatientAge(List<Integer> patientAge) {
		this.patientAge = patientAge;
	}
	public List<LocalDate> getReportFinalizedDate() {
		return reportFinalizedDate;
	}
	public void setReportFinalizedDate(List<LocalDate> reportFinalizedDate) {
		this.reportFinalizedDate = reportFinalizedDate;
	}
	public List<String> getResultStatus() {
		return resultStatus;
	}
	public void setResultStatus(List<String> resultStatus) {
		this.resultStatus = resultStatus;
	}
	public List<String> getReadingLocation() {
		return readingLocation;
	}
	public void setReadingLocation(List<String> readingLocation) {
		this.readingLocation = readingLocation;
	}
	public List<String> getPatientSex() {
		return patientSex;
	}
	public void setPatientSex(List<String> patientSex) {
		this.patientSex = patientSex;
	}
	public List<String> getExamDescription() {
		return examDescription;
	}
	public void setExamDescription(List<String> examDescription) {
		this.examDescription = examDescription;
	}
	public List<String> getModality() {
		return modality;
	}
	public void setModality(List<String> modality) {
		this.modality = modality;
	}
	
	public boolean isEmpty(){
		if(!resultStatus.isEmpty()) return false;
		if(!readingLocation.isEmpty()) return false;
		if(!patientSex.isEmpty()) return false;
		if(!examDescription.isEmpty()) return false;
		
		if(!modality.isEmpty()) return false;
		if(patientAge.size()==2) return false;
		if(reportFinalizedDate.size()==2) return false;
		return true;
	}
}
