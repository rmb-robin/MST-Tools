package com.mst.model.businessRule;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mst.jsonSerializers.ObjectIdJsonSerializer;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.List;
import java.util.Map;

/**
 * Provides the data model for serializing business rule objects to the DB.
 * This class applies to business rules that are applicable to the input and/or
 * output of a user query.
 *
 * @author Brian Sheely
 * @version %I%, %G%
 */
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
        private boolean edgeNameExists;                                 // if false, create synonym if edgeName does not exist
        private String edgeName;                                        //e.g., disease modifier
        private List<String> edgeValues;                                //e.g., small
        private String synonymousEdge;                                  //e.g., measurement
        private List<String> synonymousValues;                          //e.g., [".1cm"]
        private List<DiscreteDataType> discreteDataToMatch;
        private Map<String, List<String>> edgeValuesToMatch;            //e.g., existence, disease location["ovary", "ovarian"]

        /**
         * Gets the name of the business rule.
         * @return description of the business rule
         */
        public String getRuleName() {
            return ruleName;
        }

        /**
         * Sets the name of the business rule.
         * @param ruleName user friendly description of the business rule
         */
        public void setRuleName(String ruleName) {
            this.ruleName = ruleName;
        }

        public List<String> getQueryTokens() {
            return queryTokens;
        }

        public void setQueryTokens(List<String> queryTokens) {
            this.queryTokens = queryTokens;
        }

        public boolean isEdgeNameExists() {
            return edgeNameExists;
        }

        public void setEdgeNameExists(boolean edgeNameExists) {
            this.edgeNameExists = edgeNameExists;
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

        public List<DiscreteDataType> getDiscreteDataToMatch() {
            return discreteDataToMatch;
        }

        public void setDiscreteDataToMatch(List<DiscreteDataType> discreteDataToMatch) {
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

    /**
     * Gets the name used to further categorize this class of business rules.
     * @return String to be used in the business rule DB query
     */
    public String getRuleType() {
        return ruleType;
    }

    /**
     * Sets the unique name to further categorize this class of business rules.
     * @param ruleType String used to categorize this business rule
     * @see com.mst.model.metadataTypes.QueryBusinessRuleTypes
     */
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
