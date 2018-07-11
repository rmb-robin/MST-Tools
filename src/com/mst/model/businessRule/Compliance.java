package com.mst.model.businessRule;

import com.mst.model.discrete.FollowupRecommendation;

import java.util.List;
import java.util.Map;

import static com.mst.model.metadataTypes.ComplianceBucket.*;

public class Compliance extends BusinessRule  {
    private String ruleName;
    private Map<String, List<String>> edgesToMatch;
    private List<Bucket> buckets;

    public Compliance() {
        super(Compliance.class.getName());
    }

    public static class Bucket {
        private BucketType bucketType;
        private String bucketName;
        private double minSize;
        private double maxSize;
        private int minAge;
        private int maxAge;
        private String menopausalStatus;
        private FollowupRecommendation followupRecommendation;

        public BucketType getBucketType() {
            return bucketType;
        }

        public void setBucketType(BucketType bucketType) {
            this.bucketType = bucketType;
        }

        public String getBucketName() {
            return bucketName;
        }

        public void setBucketName(String bucketName) {
            this.bucketName = bucketName;
        }

        public double getMinSize() {
            return minSize;
        }

        public void setMinSize(double minSize) {
            this.minSize = minSize;
        }

        public double getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(double maxSize) {
            this.maxSize = maxSize;
        }

        public int getMinAge() {
            return minAge;
        }

        public void setMinAge(int minAge) {
            this.minAge = minAge;
        }

        public int getMaxAge() {
            return maxAge;
        }

        public void setMaxAge(int maxAge) {
            this.maxAge = maxAge;
        }

        public String getMenopausalStatus() {
            return menopausalStatus;
        }

        public void setMenopausalStatus(String menopausalStatus) {
            this.menopausalStatus = menopausalStatus;
        }

        public FollowupRecommendation getFollowupRecommendation() {
            return followupRecommendation;
        }

        public void setFollowupRecommendation(FollowupRecommendation followupRecommendation) {
            this.followupRecommendation = followupRecommendation;
        }
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public Map<String, List<String>> getEdgesToMatch() {
        return edgesToMatch;
    }

    public void setEdgesToMatch(Map<String, List<String>> edgesToMatch) {
        this.edgesToMatch = edgesToMatch;
    }

    public List<Bucket> getBuckets() {
        return buckets;
    }

    public void setBuckets(List<Bucket> buckets) {
        this.buckets = buckets;
    }
}
