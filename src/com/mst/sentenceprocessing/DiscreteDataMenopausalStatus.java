package com.mst.sentenceprocessing;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.mst.metadataProviders.DiscreteDataCustomFieldNames;
import com.mst.model.metadataTypes.CustomFieldDataType;
import com.mst.model.discrete.DiscreteData;
import com.mst.model.discrete.DiscreteDataCustomField;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import static com.mst.model.metadataTypes.MenopausalStatus.*;
import static java.lang.Math.toIntExact;

public class DiscreteDataMenopausalStatus {
    private static final Logger LOGGER = LogManager.getLogger(DiscreteDataMenopausalStatus.class);
    private static final int MENOPAUSAL_MIN_AGE = 45;
    private static final int MENOPAUSAL_MAX_AGE = 57;

    public static void setStatus(DiscreteData discreteData) {
        if (discreteData == null || discreteData.getSex() == null) {
            LOGGER.debug("discreteData is null or discreteData.sex is null");
            return;
        }
        if (discreteData.getSex().matches("(?i)female|f")) {
            if (discreteData.getPatientAge() == 0) {
                LocalDate dob = discreteData.getPatientDob();
                LocalDate date = discreteData.getReportFinalizedDate();
                if (dob == null || date == null) {
                    LOGGER.debug("discreteData.patientDob is null or discreteData.reportFinalizedDate is null");
                    return;
                }
                long age = dob.until(date, ChronoUnit.YEARS);
                try {
                    discreteData.setPatientAge(toIntExact(age));
                } catch (ArithmeticException e) {
                    LOGGER.debug("Exception calculating patient age");
                    return;
                }
            }
            String status = MENOPAUSAL;
            if (discreteData.getPatientAge() <= MENOPAUSAL_MIN_AGE)
                status = PREMENOPAUSAL;
            else if (discreteData.getPatientAge() >= MENOPAUSAL_MAX_AGE)
                status = POSTMENOPAUSAL;
            DiscreteDataCustomField field = new DiscreteDataCustomField(DiscreteDataCustomFieldNames.menopausalStatus, status, CustomFieldDataType.string);
            discreteData.getCustomFields().add(field);
        }
    }
}
