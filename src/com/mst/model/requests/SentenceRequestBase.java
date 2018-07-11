package com.mst.model.requests;

import com.mst.model.discrete.DiscreteData;

public abstract class SentenceRequestBase {
    private String source;
    private String practice;
    private String study;
    private DiscreteData discreteData;
    private boolean convertMeasurements;
    private boolean needResult;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getPractice() {
        return practice;
    }

    public void setPractice(String practice) {
        this.practice = practice;
    }

    public String getStudy() {
        return study;
    }

    public void setStudy(String study) {
        this.study = study;
    }

    public DiscreteData getDiscreteData() {
        return discreteData;
    }

    public void setDiscreteData(DiscreteData discreteData) {
        this.discreteData = discreteData;
    }

    public boolean isConvertMeasurements() {
        return convertMeasurements;
    }

    public void setConvertMeasurements(boolean convertMeasurements) {
        this.convertMeasurements = convertMeasurements;
    }

    public void setNeedResult(boolean needResult) {
        this.needResult = needResult;
    }
}
