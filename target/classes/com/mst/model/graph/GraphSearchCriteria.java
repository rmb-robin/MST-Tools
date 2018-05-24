package com.mst.model.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GraphSearchCriteria {
	private String searchRelationship; 
	private List<GraphSearchCriterion> search = new ArrayList<>();
	//private Map<String, String> filters = new HashMap<>();
	private Map<String, String> compliance = new HashMap<>();

	public GraphSearchCriteria() {
		searchRelationship = "and";
	}
	
	public GraphSearchCriteria(String searchRelationship) {
		this.searchRelationship = searchRelationship;
	}
	
	public String getSearchRelationship() {
		return searchRelationship;
	}
	
	public void setSearchRelationship(String searchRelationship) {
		this.searchRelationship = searchRelationship;
	}
	
	public List<GraphSearchCriterion> getSearch() {
		return search;
	}
	
	public void setSearch(List<GraphSearchCriterion> search) {
		this.search = search;
	}
	
//	public Map<String, String> getFilters() {
//		return filters;
//	}
//	
//	public void setFilters(Map<String, String> filters) {
//		this.filters = filters;
//	}
	
	public Map<String, String> getCompliance() {
		return compliance;
	}
	
	public void setCompliance(Map<String, String> compliance) {
		this.compliance = compliance;
	}	
}