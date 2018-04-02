package com.mst.model.businessRule;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mst.jsonSerializers.ObjectIdJsonSerializer;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.List;
import java.util.Map;

@Entity("queryBusinessRule")
public class QueryBusinessRule {
    @Id
    @JsonSerialize(using=ObjectIdJsonSerializer.class)
    private ObjectId id;
    private String organizationId;
    private String ruleType;    //See QueryBusinessRuleTypes

    private List<Rule> rules;
    private List<String> tokenSequenceToExlude;
    
    public static class Rule {
        private String ruleName;
        private List<String> queryTokens;                               //e.g., cyst, cysts, lesion
        private String edgeName;                                        //e.g., measurement
        private List<String> edgeValues;                                //e.g., ["0", ".3"]
        private String synonymousEdge;                                  //e.g., disease modifier
        private List<String> synonymousValues;                          //e.g., small
        private Map<String, List<String>> discreteDataToMatch;          //e.g., sex["F"], patientAge["0", "18"] NOTE: 0, 18 designates a range
        private Map<String, List<String>> edgeValuesToMatch;            //e.g., existence, disease location["ovary", "ovarian"]

        public String getRuleName() {
            return ruleName;
        }

        public void setRuleName(String ruleName) {
            this.ruleName = ruleName;
        }

        public List<String> getQueryTokens() {
            return queryTokens;
        }

        public void setQueryTokens(List<String> queryTokens) {
            this.queryTokens = queryTokens;
        }

        public String getEdgeName() {
            return edgeName;
        }

        public void setEdgeName(String edgeName) {
            this.edgeName = edgeName;
        }

        public List<String> getEdgeValues() {
            return edgeValues;
        }

        public void setEdgeValues(List<String> edgeValues) {
            this.edgeValues = edgeValues;
        }

        public String getSynonymousEdge() {
            return synonymousEdge;
        }

        public void setSynonymousEdge(String synonymousEdge) {
            this.synonymousEdge = synonymousEdge;
        }

        public List<String> getSynonymousValues() {
            return synonymousValues;
        }

        public void setSynonymousValues(List<String> synonymousValues) {
            this.synonymousValues = synonymousValues;
        }

        public Map<String, List<String>> getDiscreteDataToMatch() {
            return discreteDataToMatch;
        }

        public void setDiscreteDataToMatch(Map<String, List<String>> discreteDataToMatch) {
            this.discreteDataToMatch = discreteDataToMatch;
        }

        public Map<String, List<String>> getEdgeValuesToMatch() {
            return edgeValuesToMatch;
        }

        public void setEdgeValuesToMatch(Map<String, List<String>> edgeValuesToMatch) {
            this.edgeValuesToMatch = edgeValuesToMatch;
        }
    }



    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public List<String> getTokenSequenceToExlude() {
		return this.tokenSequenceToExlude;
	}

	public void setTokenSequenceToExlude(List<String> tokenSequenceToExlude) {
		this.tokenSequenceToExlude = tokenSequenceToExlude;
	}
}
