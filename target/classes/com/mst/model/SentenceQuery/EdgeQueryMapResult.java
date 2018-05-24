package com.mst.model.SentenceQuery;

import java.util.HashMap;
import java.util.Map;

public class EdgeQueryMapResult {

	private Map<String, EdgeQuery> namedEdges; 
	private Map<String, EdgeQuery> nonNamedEdges;
	public Map<String, EdgeQuery> getNamedEdges() {
		return namedEdges;
	}
	
	
	public EdgeQueryMapResult(){
		namedEdges = new HashMap<>();
		nonNamedEdges = new HashMap<>();
	}
	
	public void setNamedEdges(Map<String, EdgeQuery> namedEdges) {
		this.namedEdges = namedEdges;
	}
	public Map<String, EdgeQuery> getNonNamedEdges() {
		return nonNamedEdges;
	}
	public void setNonNamedEdges(Map<String, EdgeQuery> nonNamedEdges) {
		this.nonNamedEdges = nonNamedEdges;
	} 

}
