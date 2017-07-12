package com.mst.sentenceprocessing;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.mst.interfaces.sentenceprocessing.DiscreteDataNormalizer;
import com.mst.metadataProviders.DiscreteDataCustomFieldNames;
import com.mst.model.metadataTypes.CustomFieldDataType;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.discrete.DiscreteDataCustomField;

import static java.lang.Math.toIntExact;

public class DiscreteDataNormalizerImpl implements DiscreteDataNormalizer {

	private final int MENOPAUSE_LOW_CUTOFF = 45;
	private final int MENOPAUSE_HIGH_CUTOFF = 57;
	private final String MENOPAUSE_DEFAULT = "Menopausal";
	private final String MENOPAUSE_PRE = "Pre-Menopausal";
	private final String MENOPAUSE_POST = "Post-Menopausal";
	
	public DiscreteData process(DiscreteData discreteData) {
		
		calculateAge(discreteData);
		
		determineMenopausalStatus(discreteData);
		
		return discreteData;
	}
	
	private void calculateAge(DiscreteData discreteData) {
		if(discreteData.getPatientAge() == 0) {
			LocalDate dob = discreteData.getPatientDob();
			LocalDate finalized = discreteData.getReportFinalizedDate();
			
			if(dob != null && finalized != null) {
				long age = dob.until(finalized, ChronoUnit.YEARS);
				try {
					discreteData.setPatientAge(toIntExact(age));
				} catch(ArithmeticException e) {
					discreteData.setPatientAge(0);
				}
			}
		}
	}
	
	private void determineMenopausalStatus(DiscreteData discreteData) {
		if(discreteData.getPatientAge() > 0 && isFemalePatient(discreteData)) {
			String status = MENOPAUSE_DEFAULT;
				
			if(discreteData.getPatientAge() <= MENOPAUSE_LOW_CUTOFF) {
				status = MENOPAUSE_PRE;
			} else if(discreteData.getPatientAge() >= MENOPAUSE_HIGH_CUTOFF) {
				status = MENOPAUSE_POST;
			}
			
			DiscreteDataCustomField field = new DiscreteDataCustomField(DiscreteDataCustomFieldNames.menopausalStatus, status, CustomFieldDataType.string);
			discreteData.getCustomFields().add(field);
		}
	}
	
	private boolean isFemalePatient(DiscreteData discreteData) {
		if(discreteData != null && 
		   discreteData.getSex() != null && 
		   discreteData.getSex().matches("(?i)female|f")) {
			return true;
		} else {
			return false;
		}
	}
}
