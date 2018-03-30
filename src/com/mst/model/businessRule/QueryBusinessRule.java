package com.mst.model.businessRule;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mst.jsonSerializers.ObjectIdJsonSerializer;
import javafx.util.Pair;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.List;

@Entity("queryBusinessRule")
public class QueryBusinessRule {
    @Id
    @JsonSerialize(using=ObjectIdJsonSerializer.class)
    private ObjectId id;
    private String organizationId;
    private String ruleName;
    private String ruleType;                                        //See QueryBusinessRuleTypes
    private List<String> queryTokens;                               //e.g., cyst, cysts, lesion
    private String edgeName;                                        //e.g., measurement
    private List<String> edgeValues;                                //e.g., ["0", ".3"]
    private String synonymousEdge;                                  //e.g., disease modifier
    private String synonymousValue;                                 //e.g., small
    private List<Pair<String, List<String>>> edgeValuesToMatch;     //e.g., existence, disease location["ovary", "ovarian"]
    private List<String> tokenSequenceToExlude; 

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

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
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

    public String getSynonymousValue() {
        return synonymousValue;
    }

    public void setSynonymousValue(String synonymousValue) {
        this.synonymousValue = synonymousValue;
    }

    public List<Pair<String, List<String>>> getEdgeValuesToMatch() {
        return edgeValuesToMatch;
    }

    public void setEdgeValuesToMatch(List<Pair<String, List<String>>> edgeValuesToMatch) {
        this.edgeValuesToMatch = edgeValuesToMatch;
    }

	public List<String> getTokenSequenceToExlude() {
		return tokenSequenceToExlude;
	}

	public void setTokenSequenceToExlude(List<String> tokenSequenceToExlude) {
		this.tokenSequenceToExlude = tokenSequenceToExlude;
	}
}
