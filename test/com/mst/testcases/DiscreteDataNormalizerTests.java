package com.mst.testcases;

import org.junit.Test;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.discrete.DiscreteDataCustomField;
import com.mst.sentenceprocessing.DiscreteDataNormalizerImpl;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
		
		assertEquals(discrete.getCustomFields().size(), 1);
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
		
		assertEquals(discrete.getCustomFields().size(), 1);
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
		
		assertEquals(discrete.getCustomFields().size(), 1);
	}
	
	@Test
	public void testCalcAge() {
		DateTimeFormatter format = DateTimeFormatter.ofPattern("M/d/yyyy");
		DiscreteData discrete = getDiscreteObject(LocalDate.parse("2/20/1980", format), LocalDate.parse("3/1/2017", format));
		
		assertEquals(discrete.getPatientAge(), 37);
	}
	
	private DiscreteData getDiscreteObject(int age) {
		DiscreteData discrete = new DiscreteData();
		
		discrete.setPatientAge(age);
		discrete.setSex("F");
		
		DiscreteDataNormalizerImpl normalizer = new DiscreteDataNormalizerImpl();
		normalizer.process(discrete);
		
		return discrete;
	}
	
	private DiscreteData getDiscreteObject(LocalDate dob, LocalDate finalized) {
		DiscreteData discrete = new DiscreteData();
		
		discrete.setPatientDob(dob);
		discrete.setReportFinalizedDate(finalized);
		
		DiscreteDataNormalizerImpl normalizer = new DiscreteDataNormalizerImpl();
		normalizer.process(discrete);
		
		return discrete;
	}
}

		