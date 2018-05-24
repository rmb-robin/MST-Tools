package com.mst.model.graph;

import java.util.ArrayList;
import java.util.List;

public class GraphSearchCriterion {
	private List<String> tokens = new ArrayList<>();
	private List<FrameAndValue> frames = new ArrayList<>();
	private List<Filter> filters = new ArrayList<>();
	private boolean likeTerms;
	
	//private String relationship;
	
	public GraphSearchCriterion(List<String> tokens) {
		setTokens(tokens);
		//this.relationship = relationship;
	}
	
	public List<String> getTokens() {
		return tokens;
	}

	public void setTokens(List<String> tokens) {
		this.tokens = tokens;
	}

	public List<FrameAndValue> getFrames() {
		return frames;
	}

	public void setFrames(List<FrameAndValue> frames) {
		this.frames = frames;
	}

	public List<Filter> getFilters() {
		return filters;
	}

	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}
	
	public boolean getLikeTerms() {
		return likeTerms;
	}
	
	public void setLikeTerms(boolean likeTerms) {
		this.likeTerms = likeTerms;
	}
	
//	public String getRelationship() {
//		return relationship;
//	}
//
//	public void setRelationship(String relationship) {
//		this.relationship = relationship;
//	}

	public class FrameAndValue {
		private String frame;
		private List<String> values = new ArrayList<>();
		
		public FrameAndValue(String frame, List<String> values) {
			this.frame = frame;
			this.values = values;
		}
		
		public String getFrame() {
			return frame;
		}
		public void setFrame(String frame) {
			this.frame = frame;
		}
		public List<String> getValues() {
			return values;
		}
		public void setValues(List<String> values) {
			this.values = values;
		}
	}
	
	/* Filters represent an org's discreet data */
	public class Filter {
		private String field;
		private String operator;
		private List<String> values = new ArrayList<>();
		
		public Filter(String field, String operator, List<String> values) {
			this.field = field;
			this.operator = operator;
			this.values = values;
		}
		
		public String getField() {
			return field;
		}
		public void setField(String field) {
			this.field = field;
		}
		public String getOperator() {
			return operator;
		}
		public void setOperator(String operator) {
			this.operator = operator;
		}
		public List<String> getValues() {
			return values;
		}
		public void setValues(List<String> values) {
			this.values = values;
		}
	}
}
