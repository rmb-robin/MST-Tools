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
    private String ruleType;                                            //See QueryBusinessRuleTypes
    private List<Rule> rules;
    private List<String> tokenSequenceToExlude;

    public static class Rule {
        private String ruleName;
        private List<String> queryTokens;                               //e.g., cyst, cysts, lesion
        private List<Edge> edges;
        private boolean addEdgesToQuery;
        private Map<String, List<String>> edgeValuesToMatch;            //e.g., existence, disease location["ovary", "ovarian"]
        private String synonymousEdge;                                  //e.g., measurement
        private List<SynonymousEdgeValue> synonymousEdgeValues;

        public static class Edge {
            public enum LogicalOperator {AND, OR}
            private LogicalOperator logicalOperator;                    // applies to the previous edge in the collection
            private boolean edgeNameExists;                             // if false, create synonym if edgeName does not exist
            private String edgeName;                                    //e.g., disease modifier
            private String edgeValue;                                   //e.g., large
            private boolean isEdgeNumeric;

            public LogicalOperator getLogicalOperator() {
                return logicalOperator;
            }

            public void setLogicalOperator(LogicalOperator logicalOperator) {
                this.logicalOperator = logicalOperator;
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

            public String getEdgeValue() {
                return edgeValue;
            }

            public void setEdgeValue(String edgeValue) {
                this.edgeValue = edgeValue;
            }

            public boolean isEdgeNumeric() {
                return isEdgeNumeric;
            }

            public void setEdgeNumeric(boolean edgeNumeric) {
                isEdgeNumeric = edgeNumeric;
            }
        }

        public static class SynonymousEdgeValue {
            private boolean hasMinRangeValue;
            private boolean hasMaxRangeValue;
            private int minRangeValue;
            private int maxRangeValue;
            private String synonymousValue;                         //e.g., [".1cm"]

            public boolean isHasMinRangeValue() {
                return hasMinRangeValue;
            }

            public void setHasMinRangeValue(boolean hasMinRangeValue) {
                this.hasMinRangeValue = hasMinRangeValue;
            }

            public boolean isHasMaxRangeValue() {
                return hasMaxRangeValue;
            }

            public void setHasMaxRangeValue(boolean hasMaxRangeValue) {
                this.hasMaxRangeValue = hasMaxRangeValue;
            }

            public int getMinRangeValue() {
                return minRangeValue;
            }

            public void setMinRangeValue(int minRangeValue) {
                this.minRangeValue = minRangeValue;
            }

            public int getMaxRangeValue() {
                return maxRangeValue;
            }

            public void setMaxRangeValue(int maxRangeValue) {
                this.maxRangeValue = maxRangeValue;
            }

            public String getSynonymousValue() {
                return synonymousValue;
            }

            public void setSynonymousValue(String synonymousValue) {
                this.synonymousValue = synonymousValue;
            }
        }

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

        public List<Edge> getEdges() {
            return edges;
        }

        public void setEdges(List<Edge> edges) {
            this.edges = edges;
        }

        public boolean isAddEdgesToQuery() {
            return addEdgesToQuery;
        }

        public void setAddEdgesToQuery(boolean addEdgesToQuery) {
            this.addEdgesToQuery = addEdgesToQuery;
        }

        public Map<String, List<String>> getEdgeValuesToMatch() {
            return edgeValuesToMatch;
        }

        public void setEdgeValuesToMatch(Map<String, List<String>> edgeValuesToMatch) {
            this.edgeValuesToMatch = edgeValuesToMatch;
        }

        public String getSynonymousEdge() {
            return synonymousEdge;
        }

        public void setSynonymousEdge(String synonymousEdge) {
            this.synonymousEdge = synonymousEdge;
        }

        public List<SynonymousEdgeValue> getSynonymousEdgeValues() {
            return synonymousEdgeValues;
        }

        public void setSynonymousEdgeValues(List<SynonymousEdgeValue> synonymousEdgeValues) {
            this.synonymousEdgeValues = synonymousEdgeValues;
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
