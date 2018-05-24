package com.mst.model.requests;

import java.util.ArrayList;
import java.util.List;

import com.mst.model.discrete.DiscreteData;

public class IcdTenRequest{
    private boolean convertMeasurements, convertLargest;
    private DiscreteData discreteData;
    private List<IcdTenSentenceInstance> instances = new ArrayList<>();
    public void setConvertLargest(boolean convertLargest) {
        this.convertLargest = convertLargest;
    }
    public void setConvertMeasurements(boolean convertMeasurements) {
        this.convertMeasurements = convertMeasurements;
    }
    public void setDiscreteData(DiscreteData discreteData) {
        this.discreteData = discreteData;
    }
    public void setSentenceInstances(List<IcdTenSentenceInstance> instances){
        this.instances = instances;
    }
    public List<IcdTenSentenceInstance> getSentenceInstances(){
        return instances;
    }
        
}
