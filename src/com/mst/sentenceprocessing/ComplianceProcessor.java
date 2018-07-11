package com.mst.sentenceprocessing;

import com.mst.metadataProviders.DiscreteDataCustomFieldNames;
import com.mst.model.businessRule.BusinessRule;
import com.mst.model.businessRule.BusinessRule.*;
import com.mst.model.businessRule.Compliance;
import com.mst.model.businessRule.Compliance.*;
import com.mst.model.discrete.*;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.model.sentenceProcessing.TokenRelationship;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mst.model.businessRule.BusinessRule.LogicalOperator.*;
import static com.mst.model.metadataTypes.EdgeNames.*;
import static com.mst.model.metadataTypes.FollowupDescriptor.*;
import static com.mst.model.metadataTypes.UnitOfMeasure.*;

public class ComplianceProcessor {
    private static final Logger LOGGER = LogManager.getLogger(ComplianceProcessor.class);

    public ComplianceResult process(DiscreteData discreteData, List<SentenceDb> sentences, BusinessRule businessRule, boolean setFollowupRecommendation) {
        ComplianceResult result = new ComplianceResult();
        List<TokenRelationship> tokenRelationships = new ArrayList<>();
        for (SentenceDb sentence : sentences)
            tokenRelationships.addAll(sentence.getTokenRelationships());
        List<BusinessRule> rules = businessRule.getRules();
        for (BusinessRule baseRule : rules) {
            Compliance rule = (Compliance) baseRule;
            Map<String, List<String>> edgesToMatch = rule.getEdgesToMatch();
            if (areEdgesToMatchFound(tokenRelationships, edgesToMatch)) {
                List<Bucket> buckets = rule.getBuckets();
                for (Bucket bucket : buckets) {
                    if (discreteDataMatches(discreteData, bucket) && sizeMatches(tokenRelationships, bucket)) {
                        boolean isCompliant = isCompliant(tokenRelationships, bucket);
                        result.addBucketCompliance(bucket.getBucketName(), isCompliant);
                        if (setFollowupRecommendation)
                            result.setFollowupRecommendation(bucket.getFollowupRecommendation());
                    }
                }
                break;
            }
        }
        return result;
    }

    private boolean isCompliant(List<TokenRelationship> tokenRelationships, Bucket bucket) {
        FollowupRecommendation followup = bucket.getFollowupRecommendation();
        boolean processingOr = false;
        boolean foundOr = false;
        List<FollowupDescriptor> descriptors = followup.getFollowupDescriptors();
        if (descriptors != null)
        for (FollowupDescriptor followupDescriptor : descriptors) {
            LogicalOperator logicalOperator = followupDescriptor.getLogicalOperator();
            String descriptor = followupDescriptor.getDescriptor();
            if (doTokenRelationshipsContainDescriptor(tokenRelationships, descriptor) && OR.equals(logicalOperator))
                foundOr = true;
            if (!doTokenRelationshipsContainDescriptor(tokenRelationships, descriptor) && ((logicalOperator == null && !processingOr) || (!OR.equals(logicalOperator) && processingOr && !foundOr)))
                return false;
            processingOr = logicalOperator == OR;
            if (!processingOr)
                foundOr = false;
        }
        int followupTime = followup.getTime();
        boolean ongoing = followup.isOngoing(); //NOTE: No TokenRelationship exists for frequency of an event
        if (followupTime > 0) {
            String followupUnitOfMeasure = followup.getUnitOfMeasure();
            for (TokenRelationship tokenRelationship : tokenRelationships)
                if (tokenRelationship.getEdgeName().equals(time)) {
                    int time = Integer.parseInt(tokenRelationship.getFromToken().getToken());
                    String unitOfMeasure = tokenRelationship.getToToken().getToken();
                    if (followupTime == 12 && followupUnitOfMeasure.equals(MONTHS) && time == 1 && unitOfMeasure.equals(YEAR))
                        return true;
                    else if (followupTime == 1 && followupUnitOfMeasure.equals(YEAR) && time == 12 && unitOfMeasure.equals(MONTHS))
                        return true;
                    else
                        return (followupTime == time && followupUnitOfMeasure.equals(unitOfMeasure));
                } else if (tokenRelationship.getEdgeName().equals("procedure timing")) {
                    if (followupTime == 12 && followupUnitOfMeasure.equals(MONTHS) && tokenRelationship.getFromToken().getToken().equals("annual"))
                        return true;
                    else if (followupTime == 12 && followupUnitOfMeasure.equals(MONTHS) && tokenRelationship.getFromToken().getToken().equals("annually"))
                        return true;
                    else if (followupTime == 1 && followupUnitOfMeasure.equals(YEAR) && tokenRelationship.getFromToken().getToken().equals("annually"))
                        return true;
                    else if (followupTime == 1 && followupUnitOfMeasure.equals(YEAR) && tokenRelationship.getFromToken().getToken().equals("annually"))
                        return true;
                }
            return false;
        }
        return true;
    }

    private boolean doTokenRelationshipsContainDescriptor(List<TokenRelationship> tokenRelationships, String descriptor) {
        for (TokenRelationship tokenRelationship : tokenRelationships) {
            String fromToken = tokenRelationship.getFromToken().getToken();
            String toToken = tokenRelationship.getToToken().getToken();
            if (descriptor.equals(NO_FOLLOWUP) && tokenRelationship.getEdgeName().equals(existenceNo) && fromToken.equals("no") && (toToken.equals("follow") || toToken.equals("followup") || toToken.equals("follow-up")))
                return true;
            else if (fromToken.equals(descriptor) || toToken.equals(descriptor))
                return true;
            else if (descriptor.equals(fromToken + " " + toToken))
                return true;
        }
        return false;
    }

    private boolean discreteDataMatches(DiscreteData discreteData, Bucket bucket) {
        if (discreteData == null)
            return false;
        int minAge = bucket.getMinAge();
        int maxAge = bucket.getMaxAge();
        if (minAge != 0 || maxAge != 0) {
            int age = discreteData.getPatientAge();
            if (age < minAge || age > maxAge)
                return false;
        }
        String menopausalStatus = bucket.getMenopausalStatus();
        if (menopausalStatus != null) {
            List<DiscreteDataCustomField> customFields = discreteData.getCustomFields();
            Map<String, DiscreteDataCustomField> customFieldsByName = customFields.stream().collect(Collectors.toMap(DiscreteDataCustomField::getFieldName, x -> x));
            if (!customFieldsByName.containsKey(DiscreteDataCustomFieldNames.menopausalStatus))
                return false;
            return menopausalStatus.equals(customFieldsByName.get(DiscreteDataCustomFieldNames.menopausalStatus).getValue());
        }
        return true;
    }

    private boolean sizeMatches(List<TokenRelationship> tokenRelationships, Bucket bucket) {
        double minSize = bucket.getMinSize();
        double maxSize = bucket.getMaxSize();
        if (minSize > 0 || maxSize > 0) {
            for (TokenRelationship tokenRelationship : tokenRelationships)
                if (tokenRelationship.getEdgeName().equals(measurement)) {
                    double size = Double.parseDouble(tokenRelationship.getFromToken().getToken());
                    return size >= minSize && size <= maxSize;
                }
        }
        return true;
    }

    private boolean isEdgeToMatchFound(List<TokenRelationship> tokenRelationships, String edgeToMatch, List<String> edgeValues) {
        for (TokenRelationship tokenRelationship : tokenRelationships) {
            if (edgeToMatch.equals(tokenRelationship.getEdgeName()))
                if (edgeValues == null || (edgeValues.contains(tokenRelationship.getFromToken().getToken()) || edgeValues.contains(tokenRelationship.getToToken().getToken())))
                    return true;
        }
        return false;
    }

    private boolean areEdgesToMatchFound(List<TokenRelationship> tokenRelationships, Map<String, List<String>> edgesToMatch) {
        if (tokenRelationships == null || tokenRelationships.isEmpty())
            return false;
        for (Map.Entry<String, List<String>> entry : edgesToMatch.entrySet()) {
            List<String> values = entry.getValue();
            if (values != null && !values.isEmpty() && !isEdgeToMatchFound(tokenRelationships, entry.getKey(), values))
                return false;
            else if (!isEdgeToMatchFound(tokenRelationships, entry.getKey(), null))
                return false;
        }
        return true;
    }
}
