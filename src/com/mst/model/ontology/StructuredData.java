package com.mst.model.ontology;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class StructuredData {
	public String patientID;
	public Date date = new Date(0);
	public String practice;
	public String study;
	// related to a verb phrase
	public List<Multimap<String, String>> related = new ArrayList<Multimap<String, String>>();
	// unrelated to a verb phrase; i.e., appears in a noun or prep phrase that cannot be grammatically related (by current metadata) to a verb phrase
	public Multimap<String, String> unrelated = ArrayListMultimap.create();
	public String sentence;
}
