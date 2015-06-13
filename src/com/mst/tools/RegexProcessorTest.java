package com.mst.tools;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Multimap;
import com.mst.model.MapValue;

public class RegexProcessorTest {

	List<Multimap<String, MapValue>> list = new ArrayList<Multimap<String, MapValue>>();
	
	@Test
	public final void testProcess() {
		RegexProcessor tester = new RegexProcessor();
		
		tester.process(list, "A follow up PSA was 52.32 and testosterone 0 from 11/11/2013.");
		    assertEquals("Diagnostic Procedure must be PSA", "PSA", getMapValueObject("Diagnostic Procedure", 0).value);
		    assertEquals("Absolute Value must be 52.32", "52.32", getMapValueObject("Absolute Value", 0).value);
	    
	    list.clear();
	    tester.process(list, "Initial presentation was for Elevated PSA (8.0 on 7/8/2014).");
		    assertEquals("Absolute Value must be 8.0", "8.0", getMapValueObject("Absolute Value", 0).value);
		    assertEquals("Known Event Date must be 7/8/2014", "7/8/2014", getMapValueObject("Known Event Date", 0).value);
	    
	    list.clear();
	    tester.process(list, "PSA=0.8 from 1/27/14; PSA=0.6 ng/ml collected 1/25/13.");
		    assertEquals("Absolute Value must be 0.8", "0.8", getMapValueObject("Absolute Value", 0).value);
		    assertEquals("Known Event Date must be 1/27/14", "1/27/14", getMapValueObject("Known Event Date", 0).value);
		    
		    assertEquals("Absolute Value must be 0.6", "0.6", getMapValueObject("Absolute Value", 1).value);
		    assertEquals("Known Event Date must be 1/25/13", "1/25/13", getMapValueObject("Known Event Date", 1).value);
	    
	    list.clear();
	    tester.process(list, "PSA (Most Recent) (5.4)");
	    	assertEquals("Absolute Value must be 5.4", "5.4", getMapValueObject("Absolute Value", 0).value);
	    
	    list.clear();
	    tester.process(list, "PSA (Most Recent) (undetectable)");
	    	assertEquals("Absolute Value must be undetectable", "undetectable", getMapValueObject("Absolute Value", 0).value);
	    
	    list.clear();
	    tester.process(list, "PSA (2.5)");
	    	assertEquals("Absolute Value must be 2.5", "2.5", getMapValueObject("Absolute Value", 0).value);
	    
	    list.clear();
	    tester.process(list, "11/11/11: --> PSA= 2.5..");
		    assertEquals("Absolute Value must be 2.5", "2.5", getMapValueObject("Absolute Value", 0).value);
		    assertEquals("Known Event Date must be 11/11/11", "11/11/11", getMapValueObject("Known Event Date", 0).value);
	    
	    list.clear();
	    tester.process(list, "03/04/11: PSA=2.6ng/ml.");
		    assertEquals("Absolute Value must be 2.6", "2.6", getMapValueObject("Absolute Value", 0).value);
		    assertEquals("Known Event Date must be 03/04/11", "03/04/11", getMapValueObject("Known Event Date", 0).value);
	    
	    list.clear();
	    tester.process(list, "PSA (22.4 ng/ml on 8/20/2010");
		    assertEquals("Absolute Value must be 22.4", "22.4", getMapValueObject("Absolute Value", 0).value);
		    assertEquals("Known Event Date must be 8/20/2010", "8/20/2010", getMapValueObject("Known Event Date", 0).value);
	    
	    list.clear();
	    tester.process(list, "PSA 0.25 on 8/14/13");
		    assertEquals("Absolute Value must be 0.25", "0.25", getMapValueObject("Absolute Value", 0).value);
		    assertEquals("Known Event Date must be 8/14/13", "8/14/13", getMapValueObject("Known Event Date", 0).value);
	    
	    list.clear();
	    tester.process(list, "PSA 0.6 ng/ml 7/12");
		    assertEquals("Absolute Value must be 0.6", "0.6", getMapValueObject("Absolute Value", 0).value);
		    assertEquals("Known Event Date must be 7/12", "7/12", getMapValueObject("Known Event Date", 0).value);
	    
	    list.clear();
	    tester.process(list, "The result was a PSA level of 37 ng/ml.");
		    assertEquals("Absolute Value must be 37", "37", getMapValueObject("Absolute Value", 0).value);

		/* Gleason */
	    list.clear();
	    tester.process(list, "TNM Staging - T: T1c Gleason score: 7 (3+4 in RA 47%, RLA 43%;");
	    	assertEquals("Diagnostic Procedure must be Gleason", "Gleason", getMapValueObject("Diagnostic Procedure", 0).value);
		    assertEquals("Absolute Value must be 7", "7", getMapValueObject("Absolute Value", 0).value);
		    
	    list.clear();
	    tester.process(list, "Path GG 3+4 10% negative margins.");
		    assertEquals("Absolute Value must be 7", "7", getMapValueObject("Absolute Value", 0).value);
	
	    list.clear();
	    tester.process(list, "adenocarcinoma in 11/12 cores with a Gleason score 4+4=8 in two cores");
		    assertEquals("Absolute Value must be 8", "8", getMapValueObject("Absolute Value", 0).value);
		    
	    list.clear();
	    tester.process(list, "Gleason grade 2 + 4 = 6");
		    assertEquals("Absolute Value must be 6", "6", getMapValueObject("Absolute Value", 0).value);
	
	    list.clear();
	    tester.process(list, "biopsy (4/12 POS BX GLEASON 6(4/14)), cystoscopy");
		    assertEquals("Absolute Value must be 6", "6", getMapValueObject("Absolute Value", 0).value);
	    
	    list.clear();
	    tester.process(list, "gleason37");
		    assertEquals("Absolute Value must be 37", "37", getMapValueObject("Absolute Value", 0).value);
		    
	}

	private MapValue getMapValueObject(String attr, int idx) {
		MapValue[] value = list.get(idx).get(attr).toArray(new MapValue[list.get(idx).get(attr).size()]);
		return value[0];
	}
}
