package com.mst.model.requests;

import com.mst.model.sentenceProcessing.DiscreteData;

public abstract class SentenceRequestBase {

	private String source, practice, study;
	private DiscreteData discreteData;
	

	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getPractice() {
		return practice;
	}
	public void setPractice(String practice) {
		this.practice = practice;
	}
	public String getStudy() {
		return study;
	}
	public void setStudy(String study) {
		this.study = study;
	}
	public DiscreteData getDiscreteData() {
		return discreteData;
	}
	public void setDiscreteData(DiscreteData discreteData) {
		this.discreteData = discreteData;
	}

	
}
