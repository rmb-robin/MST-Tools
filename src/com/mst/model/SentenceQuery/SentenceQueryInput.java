package com.mst.model.SentenceQuery;

import java.util.ArrayList;
import java.util.List;

public class SentenceQueryInput {

	private List<SentenceQueryInstance> sentenceQueryInstances; 
	private String organizationId; 
	private List<DiscreteDataFilter> discreteDataFilters;
	
	public SentenceQueryInput(){
		sentenceQueryInstances = new ArrayList<>();
		discreteDataFilters = new ArrayList<>();
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

	public void setOrganizationId(String organizationId) {
		this.organizationId = organizationId;
	}

	public List<DiscreteDataFilter> getDiscreteDataFilters() {
		return discreteDataFilters;
	}

	public void setDiscreteDataFilters(List<DiscreteDataFilter> discreteDataFilters) {
		this.discreteDataFilters = discreteDataFilters;
	}
	
	
}
