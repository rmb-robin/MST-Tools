package com.mst.model.SentenceQuery;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mst.jsonSerializers.ObjectIdJsonSerializer;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.ArrayList;
import java.util.List;

@Entity("sentenceQueryInput")
public class SentenceQueryInput {
    @Id
    @JsonSerialize(using = ObjectIdJsonSerializer.class)
    private ObjectId id;
    private boolean isNotAndAll;
    private List<SentenceQueryInstance> sentenceQueryInstances;
    private String organizationId;
    private DiscreteDataFilter discreteDataFilter;
    private boolean debug = false;
    private boolean filterByReport = false;
    private boolean filterByTokenSequence;

    public SentenceQueryInput() {
        sentenceQueryInstances = new ArrayList<>();
    }

    public List<SentenceQueryInstance> getSentenceQueryInstances() {
        return sentenceQueryInstances;
    }

    public void setSentenceQueryInstances(List<SentenceQueryInstance> sentenceQueryInstances) {
        this.sentenceQueryInstances = sentenceQueryInstances;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public boolean getDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isFilterByReport() {
        return filterByReport;
    }

    public void setFilterByReport(boolean filterByReport) {
        this.filterByReport = filterByReport;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public DiscreteDataFilter getDiscreteDataFilter() {
        return discreteDataFilter;
    }

    public void setDiscreteDataFilters(DiscreteDataFilter discreteDataFilter) {
        this.discreteDataFilter = discreteDataFilter;
    }

    public boolean getIsNotAndAll() {
        return isNotAndAll;
    }

    public void setNotAndAll(boolean isNotAndAll) {
        this.isNotAndAll = isNotAndAll;
    }

    public boolean isFilterByTokenSequence() {
        return filterByTokenSequence;
    }

    public void setFilterByTokenSequence(boolean filterByTokenSequence) {
        this.filterByTokenSequence = filterByTokenSequence;
    }
}
