package com.mst.model;

import java.util.ArrayList;
import java.util.List;

import com.mst.model.raw.HL7Element;

public class HL7FreeText {
	private String delimiter = " "; // the delimiter to output when grouping multiple HL7 fields into one free text body
	List<HL7Element> hl7Elements = new ArrayList<>();
	
	public HL7FreeText() { }
	
	public HL7FreeText(String delimiter) {
		this.delimiter = delimiter;
	}
	
	public String getDelimiter() {
		return delimiter;
	}
	
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
	
	public List<HL7Element> getHl7Elements() {
		return hl7Elements;
	}
	
	public void setHl7Elements(List<HL7Element> hl7Elements) {
		this.hl7Elements = hl7Elements;
	}
}
