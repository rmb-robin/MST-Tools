package com.mst.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.mst.util.Constants;
import com.mst.util.GsonFactory;

public class StructuredData2_0 {
	public String patientId;
	public Date date = new Date(0);
	public String practice;
	public String study;
	public Map<String, String> discreet = new HashMap<>();
	public Map<String, Object> metadata = new HashMap<>();
	public List<Finding> findings = new ArrayList<>();
	//public Map<String, String> flat = new HashMap<>(); // a flat representation of the findings. used for reporting so one doesn't have to traverse the hierarchical List.
	public Multimap<String, ?> flat = ArrayListMultimap.create(); // a flat representation of the findings. used for reporting so one doesn't have to traverse the hierarchical List.
	public String sentence;
	public String notation;
	
	public StructuredData2_0() { }
	
	public static StructuredData2_0 getInstance(StructuredData2_0 in) {
		StructuredData2_0 out = new StructuredData2_0();
		
		out.patientId = in.patientId;
		out.practice = in.practice;
		out.study = in.study;
		out.date = in.date;
		out.sentence = in.sentence;
		out.discreet = in.discreet;
		
		return out;
	}
	
	public String getNotationStringForAllFindings(Constants.StructuredNotationReturnValue returnValue) {
		String temp = "";
		
		for(Finding finding : findings) {
			temp += finding.getNotationString(returnValue) + ";";
		}
		
		return temp;
	}
	
	public void persist() throws Exception {
		try {
			if(!this.findings.isEmpty()) {
				Gson gson = GsonFactory.build();
				String json = gson.toJson(this);
				System.out.print(json);
				DBCollection coll = Constants.MongoDB.INSTANCE.getCollection("structured");
				DBObject dbObject = (DBObject) JSON.parse(json);
				coll.insert(dbObject);
			}
		} catch(Exception e) {
			throw new Exception("StructuredData2_0.persist()", e);
		}
	}
	/*
	public String getNotationStringOld() {
		String temp = "";
		
		for(Finding finding : findings) {
			temp += normalizeType(finding.type) + ": " + normalizeValue(finding.value);
			temp += processChildren("", finding) + "; ";
		}
		
		return temp;
	}
	
	private String processChildren(String temp, Finding finding) {
		for(int i=0; i < finding.children.size(); i++) {
			if(i==0)
				temp += " [";
			Finding child = finding.children.get(i);
			temp += normalizeType(child.type) + ": " + normalizeValue(child.value); 
			temp = processChildren(temp, child) + (i < finding.children.size()-1 ? ", " : "]");
		}
		
		return temp;
	}
	
	private String normalizeType(String type) {
		String val = "";
		for(String word : type.split(" ")) {
			val += word.substring(0,1);
		}
		return val.toUpperCase();
	}
	
	private String normalizeValue(Object value) {
		return value.toString().toLowerCase();
	}
	*/
}
