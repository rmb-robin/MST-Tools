package com.mst.sentenceprocessing;

import org.joda.time.DateTime;
import org.joda.time.Period;

import com.mst.interfaces.sentenceprocessing.DiscreteDataNormalizer;
import com.mst.model.metadataTypes.CustomFieldDataType;
import com.mst.model.sentenceProcessing.DiscreteData;
import com.mst.model.sentenceProcessing.DiscreteDataCustomField;

public class DiscreteDataNormalizerImpl implements DiscreteDataNormalizer {

	private final int MENOPAUSE_LOW_CUTOFF = 45;
	private final int MENOPAUSE_HIGH_CUTOFF = 57;
	private final String MENOPAUSE_DEFAULT = "Menopausal";
	private final String MENOPAUSE_PRE = "Pre-Menopausal";
	private final String MENOPAUSE_POST = "Post-Menopausal";
	
	public DiscreteData process(DiscreteData discreteData){
		if(discreteData.getPatientAge() == 0) {
			DateTime dob = discreteData.getPatientDob();
			DateTime finalized = discreteData.getReportFinalizedDate();
			
			// https://medicalsearchtechnologies.atlassian.net/browse/UI-141
			if(dob != null && finalized != null) {
				Period period = new Period(dob, finalized);
				discreteData.setPatientAge(period.getYears());
			}
			
			// https://medicalsearchtechnologies.atlassian.net/browse/UI-143
			if(isFemalePatient(discreteData)) {
				String status = MENOPAUSE_DEFAULT;
				
				if(discreteData.getPatientAge() <= MENOPAUSE_LOW_CUTOFF) {
					status = MENOPAUSE_PRE;
				} else if(discreteData.getPatientAge() >= MENOPAUSE_HIGH_CUTOFF) {
					status = MENOPAUSE_POST;
				}
				
				DiscreteDataCustomField field = new DiscreteDataCustomField("MenopausalStatus", status, CustomFieldDataType.string);
				discreteData.getCustomFields().add(field);
			}
		}
		
		return discreteData;
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
