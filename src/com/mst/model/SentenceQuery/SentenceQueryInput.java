package com.mst.model.SentenceQuery;

import java.util.ArrayList;
import java.util.List;

public class SentenceQueryInput {

	private boolean isNotAndAll;
	
	private List<SentenceQueryInstance> sentenceQueryInstances; 
	private String organizationId; 
	private DiscreteDataFilter discreteDataFilter;
	private String debug = "false";
	private boolean filterByReport = false;
	private boolean filterByTokenSequence; 
	
	
	public SentenceQueryInput(){
		sentenceQueryInstances = new ArrayList<>();
	}
	
	public List<SentenceQueryInstance> getSentenceQueryInstances() {
		return sentenceQueryInstances;
	}
	public void setSentenceQueryInstances(List<SentenceQueryInstance> sentenceQueryInstances) {
		this.sentenceQueryInstances = sentenceQueryInstances;
	}

	public String getOrganizationId() {
		return organizationId;
	}

	public String getDebug() {
		return debug;
	}

	public void setDebug(String debug) {
		this.debug = debug;
	}

	public boolean isFilterByReport() {
		return filterByReport;
	}

	public void setFilterByReport(boolean filterByReport) {
		this.filterByReport = filterByReport;
	}

	public void setOrganizationId(String organizationId) {
		this.organizationId = organizationId;
	}

	public DiscreteDataFilter getDiscreteDataFilter() {
		return discreteDataFilter;
	}

	public void setDiscreteDataFilters(DiscreteDataFilter discreteDataFilter) {
		this.discreteDataFilter = discreteDataFilter;
	}

	public boolean getIsNotAndAll() {
		return isNotAndAll;
	}

	public void setNotAndAll(boolean isNotAndAll) {
		this.isNotAndAll = isNotAndAll;
	}

	public boolean isDebug() {
		if ( debug == null ) 
			return false;
		try {
			return Boolean.parseBoolean(debug);
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isFilterByTokenSequence() {
		return filterByTokenSequence;
	}

	public void setFilterByTokenSequence(boolean filterByTokenSequence) {
		this.filterByTokenSequence = filterByTokenSequence;
	}
}
