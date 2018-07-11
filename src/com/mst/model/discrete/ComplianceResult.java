package com.mst.model.discrete;

import java.util.LinkedHashMap;
import java.util.Map;

public class ComplianceResult {
    private Map<String, Boolean> bucketCompliance;
    private FollowupRecommendation followupRecommendation;

    public ComplianceResult() {
        bucketCompliance = new LinkedHashMap<>();
    }

    public Map<String, Boolean> getBucketCompliance() {
        return bucketCompliance;
    }

    public void addBucketCompliance(String bucketName, boolean isCompliant) {
        bucketCompliance.put(bucketName, isCompliant);
    }

    public FollowupRecommendation getFollowupRecommendation() {
        return followupRecommendation;
    }

    public void setFollowupRecommendation(FollowupRecommendation followupRecommendation) {
        this.followupRecommendation = followupRecommendation;
    }
}
