package com.mst.model;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import com.mst.model.raw.HL7Element;

@Entity("hl7Details")
public class HL7Details {
	
	@Id
	private ObjectId id;
	private String org;
	private HL7Element patientId;
	private HL7Element date;
	private HL7FreeText text = new HL7FreeText();
	private List<HL7Element> discrete = new ArrayList<>();
	private boolean convertMeasurements = true;
	private boolean convertLargest = true;
	
	public ObjectId getId() {
		return id;
	}
	
	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getOrg() {
		return org;
	}
	
	public void setOrg(String org) {
		this.org = org;
	}
	
	public HL7Element getPatientId() {
		return patientId;
	}
	
	public void setPatientId(HL7Element patientId) {
		this.patientId = patientId;
	}
	
	public HL7Element getDate() {
		return date;
	}
	
	public void setDate(HL7Element date) {
		this.date = date;
	}
	
	public HL7FreeText getText() {
		return text;
	}
	
	public void setText(HL7FreeText text) {
		this.text = text;
	}
	
	public List<HL7Element> getDiscrete() {
		return discrete;
	}
	
	public void setDiscrete(List<HL7Element> discrete) {
		this.discrete = discrete;
	}

	public boolean isConvertMeasurements() {
		return convertMeasurements;
	}

	public void setConvertMeasurements(boolean convertMeasurements) {
		this.convertMeasurements = convertMeasurements;
	}

	public boolean isConvertLargest() {
		return convertLargest;
	}

	public void setConvertLargest(boolean convertLargest) {
		this.convertLargest = convertLargest;
	}
}