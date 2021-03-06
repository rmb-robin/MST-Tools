package com.mst.model.discrete;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mst.util.LocalDateConverter;
import com.mst.jsonSerializers.ObjectIdJsonSerializer;

@Entity("discreteData")
@Converters(LocalDateConverter.class)
public class DiscreteData {
    @Id
    @JsonSerialize(using = ObjectIdJsonSerializer.class)
    private ObjectId id;
    private String readingLocation;
    private String patientMRN;
    private LocalDate patientDob;
    private String sex;
    private String patientAccount;
    private String patientEncounter;
    private String vrReportId;
    private String accessionNumber;
    private String examDescription;
    private String modality;
    private String resultStatus;
    private String reportFinalizedBy;
    private String reportFinalizedById;
    private LocalDate reportFinalizedDate;
    private int patientAge;
    private FollowupRecommendation followupRecommendation;
    private LocalDate processingDate;
    private String organizationId;
    private String bucketName;
    private String isCompliant;
    private List<DiscreteDataCustomField> customFields;
    private String orderControl;
    private Boolean isDuplicate;
    private String rawFileId;
    private String parseReportId;
    private String patientName;
    private String PatientClass;
    private String principalResultInterpreterID;
    private String principalResultInterpreterName;
    private String assignedPatientLocation;
    private String orderingProviderId;
    private String orderingProviderName;
    private Map<String, String> allAvailableFields;

    public DiscreteData() {
        customFields = new ArrayList<>();
    }

    public String getReportFinalizedById() {
        return reportFinalizedById;
    }

    public void setReportFinalizedById(String reportFinalizedById) {
        this.reportFinalizedById = reportFinalizedById;
    }

    public String getOrderControl() {
        return orderControl;
    }

    public void setOrderControl(String orderControl) {
        this.orderControl = orderControl;
    }

    public String getReadingLocation() {
        return readingLocation;
    }

    public void setReadingLocation(String readingLocation) {
        this.readingLocation = readingLocation;
    }

    public String getPatientMRN() {
        return patientMRN;
    }

    public void setPatientMRN(String patientMRN) {
        this.patientMRN = patientMRN;
    }

    public LocalDate getPatientDob() {
        return patientDob;
    }

    public void setPatientDob(LocalDate patientDob) {
        this.patientDob = patientDob;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getPatientAccount() {
        return patientAccount;
    }

    public void setPatientAccount(String patientAccount) {
        this.patientAccount = patientAccount;
    }

    public String getPatientEncounter() {
        return patientEncounter;
    }

    public void setPatientEncounter(String patientEncounter) {
        this.patientEncounter = patientEncounter;
    }

    public String getVrReportId() {
        return vrReportId;
    }

    public void setVrReportId(String vrReportId) {
        this.vrReportId = vrReportId;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public String getExamDescription() {
        return examDescription;
    }

    public void setExamDescription(String examDescription) {
        this.examDescription = examDescription;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getReportFinalizedBy() {
        return reportFinalizedBy;
    }

    public void setReportFinalizedBy(String reportFinalizedBy) {
        this.reportFinalizedBy = reportFinalizedBy;
    }

    public LocalDate getReportFinalizedDate() {
        return reportFinalizedDate;
    }

    public void setReportFinalizedDate(LocalDate reportFinalizedDate) {
        this.reportFinalizedDate = reportFinalizedDate;
    }

    public int getPatientAge() {
        return patientAge;
    }

    public void setPatientAge(int patientAge) {
        this.patientAge = patientAge;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public List<DiscreteDataCustomField> getCustomFields() {
        return customFields;
    }

    public void setCustomFields(List<DiscreteDataCustomField> customFields) {
        this.customFields = customFields;
    }

    public void setTimeStamps() {
        this.processingDate = LocalDate.now();
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public LocalDate getProcessingDate() {
        return processingDate;
    }

    public void setProcessingDate(LocalDate processingDate) {
        this.processingDate = processingDate;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getIsCompliant() {
        return isCompliant;
    }

    public void setIsCompliant(String isCompliant) {
        this.isCompliant = isCompliant;
    }

    public Boolean getIsDuplicate() {
        return isDuplicate;
    }

    public void setIsDuplicate(Boolean isDuplicate) {
        this.isDuplicate = isDuplicate;
    }

    public FollowupRecommendation getFollowupRecommendation() {
        return followupRecommendation;
    }

    public void setFollowupRecommendation(FollowupRecommendation followupRecommendation) {
        this.followupRecommendation = followupRecommendation;
    }

    public String getRawFileId() {
        return rawFileId;
    }

    public void setRawFileId(String rawFileId) {
        this.rawFileId = rawFileId;
    }

    public Map<String, String> getAllAvailableFields() {
        return allAvailableFields;
    }

    public void setAllAvailableFields(Map<String, String> allAvailableFields) {
        this.allAvailableFields = allAvailableFields;
    }

    public String getParseReportId() {
        return parseReportId;
    }

    public void setParseReportId(String parseReportId) {
        this.parseReportId = parseReportId;
    }

    public String getPatientClass() {
        return PatientClass;
    }

    public void setPatientClass(String patientClass) {
        PatientClass = patientClass;
    }

    public String getPrincipalResultInterpreterID() {
        return principalResultInterpreterID;
    }

    public void setPrincipalResultInterpreterID(String principalResultInterpreterID) {
        this.principalResultInterpreterID = principalResultInterpreterID;
    }

    public String getAssignedPatientLocation() {
        return assignedPatientLocation;
    }

    public void setAssignedPatientLocation(String assignedPatientLocation) {
        this.assignedPatientLocation = assignedPatientLocation;
    }

    public String getOrderingProviderId() {
        return orderingProviderId;
    }

    public void setOrderingProviderId(String orderingProviderId) {
        this.orderingProviderId = orderingProviderId;
    }

    public void setCompliant(String isCompliant) {
        this.isCompliant = isCompliant;
    }

    public String getPrincipalResultInterpreterName() {
        return principalResultInterpreterName;
    }

    public void setPrincipalResultInterpreterName(String principalResultInterpreterName) {
        this.principalResultInterpreterName = principalResultInterpreterName;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getOrderingProviderName() {
        return orderingProviderName;
    }

    public void setOrderingProviderName(String orderingProviderName) {
        this.orderingProviderName = orderingProviderName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DiscreteData) {
            DiscreteData that = (DiscreteData) obj;
            return Objects.equals(id, that.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
