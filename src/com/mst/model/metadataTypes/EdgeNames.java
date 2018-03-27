package com.mst.model.metadataTypes;

import java.util.HashSet;

public class EdgeNames {
	public final static String unitOfMeasure = "unit of measure";
	public final static String measurement = "measurement";
	public final static String existence = "existence";
	public final static String negation = "negation";
	public final static String possibility = "possibility";
	public final static String existenceNo = "existence-no";
	public final static String existencePossibility = "existence-possibility";
	public final static String existenceMaybe = "existence-maybe";
	public final static String suppcare = "supp care";
	public final static String time = "time";
	public final static String simpleCystModifiers = "simple cyst modifiers";
	public final static String simpleCystModifier = "simple cyst modifier";
	
	public final static String diseaseModifier = "disease modifier";
	public final static String diseaseLocation = "disease location";

	public static final String hetrogeneous_finding_sites = "heterogeneous finding site";
	public static final String enlarged_finding_sites = "enlarged finding site";

	public static HashSet<String> getExistenceSet(){
		HashSet<String> result = new HashSet<String>();
		result.add(EdgeNames.existence);
		result.add(EdgeNames.existenceNo);
		result.add(EdgeNames.existencePossibility);
		result.add(EdgeNames.existenceMaybe);
		return result; 
	}
	
	public static HashSet<String> getNonExistenceSetOnly(){
		HashSet<String> result = new HashSet<String>();
		result.add(EdgeNames.existenceNo);
		result.add(EdgeNames.existencePossibility);
		result.add(EdgeNames.existenceMaybe);
		return result; 
	}
}
