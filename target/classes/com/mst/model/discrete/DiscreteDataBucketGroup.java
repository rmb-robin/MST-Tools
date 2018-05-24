package com.mst.model.discrete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class DiscreteDataBucketGroup {

	private List<ComplianceDisplayFieldsBucketItem> bucketItems;
	private Map<String,HashSet<String>> matchedEdges;
	
	public DiscreteDataBucketGroup(){
		bucketItems = new ArrayList<>();
		matchedEdges = new HashMap<>();
	}

	public List<ComplianceDisplayFieldsBucketItem> getBucketItems() {
		return bucketItems;
	}

	public void setBucketItems(List<ComplianceDisplayFieldsBucketItem> bucketItems) {
		this.bucketItems = bucketItems;
	}

	public Map<String, HashSet<String>> getMatchedEdges() {
		return matchedEdges;
	}

	public void setMatchedEdges(Map<String, HashSet<String>> matchedEdges) {
		this.matchedEdges = matchedEdges;
	}
}
