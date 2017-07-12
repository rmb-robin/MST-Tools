package com.mst.testcases;

import org.junit.Test;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.discrete.DiscreteDataCustomField;
import com.mst.sentenceprocessing.DiscreteDataNormalizerImpl;

import static org.junit.Assert.*;

import org.joda.time.DateTime;

public class DiscreteDataNormalizerTests {
	
	private final String STATUS = "MenopausalStatus";
	
	@Test
	public void testPreMenopausal() {
		DiscreteData discrete = getDiscreteObject(27);
		
		for(DiscreteDataCustomField field : discrete.getCustomFields()) {
			if(field.getFieldName().equalsIgnoreCase(STATUS)) {
				assertEquals(field.getValue(), "Pre-Menopausal");
				break;
			}
		}
	}
	
	@Test
	public void testMenopausal() {
		DiscreteData discrete = getDiscreteObject(50);
		
		for(DiscreteDataCustomField field : discrete.getCustomFields()) {
			if(field.getFieldName().equalsIgnoreCase(STATUS)) {
				assertEquals(field.getValue(), "Menopausal");
				break;
			}
		}
	}
	
	@Test
	public void testPostMenopausal() {
		DiscreteData discrete = getDiscreteObject(70);
		
		for(DiscreteDataCustomField field : discrete.getCustomFields()) {
			if(field.getFieldName().equalsIgnoreCase(STATUS)) {
				assertEquals(field.getValue(), "Post-Menopausal");
				
				break;
			}
		}
	}
	
	@Test
	public void testCalcAge() {
		DiscreteData discrete = getDiscreteObject(new DateTime(1980, 2, 20, 0, 0, 0, 0), new DateTime(2017, 3, 1, 0, 0, 0, 0));
		
		assertEquals(discrete.getPatientAge(), 37);
	}
	
	private DiscreteData getDiscreteObject(int age) {
		DiscreteData discrete = new DiscreteData();
		
		discrete.setPatientAge(age);
		
		DiscreteDataNormalizerImpl normalizer = new DiscreteDataNormalizerImpl();
		normalizer.process(discrete);
		
		return discrete;
	}
	
	private DiscreteData getDiscreteObject(DateTime dob, DateTime finalized) {
		DiscreteData discrete = new DiscreteData();
		
	//	discrete.setPatientDob(dob);
		//discrete.setReportFinalizedDate(finalized);
		
		DiscreteDataNormalizerImpl normalizer = new DiscreteDataNormalizerImpl();
		normalizer.process(discrete);
		
		return discrete;
	}
}

		