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
	
	private List<String> patientMRN; 
	private List<String> patientAccount; 
	private List<String> patientEncounter; 
	private List<String> vrReportId;
	private List<String> accessionNumber; 
	private List<LocalDate> processingDate; 
	private List<String> bucketName;
	private Boolean isComplaint; 
	private List<String> menopausalStatus;
	private List<LocalDate>	patientDob;
	private List<String> reportFinalizedBy; 
	
	
	//questions. is processing DATE a rang should we epxcet to?
	//report finailzied by or id?
	// Report Finalized By,   
	

	public DiscreteDataFilter(){
		resultStatus = new ArrayList<>();
		readingLocation = new ArrayList<>();
		patientSex = new ArrayList<>();
		examDescription = new ArrayList<>();
		modality = new ArrayList<>();
		patientAge = new ArrayList<>();
		reportFinalizedDate = new ArrayList<>();
		
		 patientMRN = new ArrayList<>(); 
		 patientAccount = new ArrayList<>(); 
		 patientEncounter = new ArrayList<>(); 
		 vrReportId = new ArrayList<>();
		 accessionNumber = new ArrayList<>(); 
		 processingDate = new ArrayList<>(); 
		 bucketName = new ArrayList<>();
		 menopausalStatus = new ArrayList<>();
		 patientDob  = new ArrayList<>();
		 reportFinalizedBy = new ArrayList<>();
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


	public List<String> getPatientMRN() {
		return patientMRN;
	}


	public void setPatientMRN(List<String> patientMRN) {
		this.patientMRN = patientMRN;
	}


	public List<String> getPatientAccount() {
		return patientAccount;
	}


	public void setPatientAccount(List<String> patientAccount) {
		this.patientAccount = patientAccount;
	}


	public List<String> getPatientEncounter() {
		return patientEncounter;
	}


	public void setPatientEncounter(List<String> patientEncounter) {
		this.patientEncounter = patientEncounter;
	}


	public List<String> getVrReportId() {
		return vrReportId;
	}


	public void setVrReportId(List<String> vrReportId) {
		this.vrReportId = vrReportId;
	}


	public List<String> getAccessionNumber() {
		return accessionNumber;
	}


	public void setAccessionNumber(List<String> accessionNumber) {
		this.accessionNumber = accessionNumber;
	}


	public List<LocalDate> getProcessingDate() {
		return processingDate;
	}


	public void setProcessingDate(List<LocalDate> processingDate) {
		this.processingDate = processingDate;
	}


	public List<String> getBucketName() {
		return bucketName;
	}


	public void setBucketName(List<String> bucketName) {
		this.bucketName = bucketName;
	}


	public Boolean getIsComplaint() {
		return isComplaint;
	}


	public void setIsComplaint(Boolean isComplaint) {
		this.isComplaint = isComplaint;
	}


	public List<String> getMenopausalStatus() {
		return menopausalStatus;
	}


	public void setMenopausalStatus(List<String> menopausalStatus) {
		this.menopausalStatus = menopausalStatus;
	}


	public List<LocalDate> getPatientDob() {
		return patientDob;
	}


	public void setPatientDob(List<LocalDate> patientDob) {
		this.patientDob = patientDob;
	}


	public List<String> getReportFinalizedBy() {
		return reportFinalizedBy;
	}


	public void setReportFinalizedBy(List<String> reportFinalizedBy) {
		this.reportFinalizedBy = reportFinalizedBy;
	}
}
