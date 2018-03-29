package com.mst.model.businessRule;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mst.jsonSerializers.ObjectIdJsonSerializer;
import javafx.util.Pair;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.List;


enum RuleType {
    CREATE_SYNONYM { public String toString() { return "createSynonym"; } }     //Implies using an OR in the query
}

@Entity("businessRule")
public class businessRule {
    @Id
    @JsonSerialize(using=ObjectIdJsonSerializer.class)
    private ObjectId id;
    private String organizationId;
    private String ruleName;
    private RuleType ruleType;
    private String edgeName;                                        //e.g., measurement
    private List<String> edgeValues;                                //e.g., ["0", "3"]
    private String synonymousEdge;                                  //e.g., disease modifier
    private String synonymousValue;                                 //e.g., small
    private List<Pair<String, List<String>>> edgeValuesToMatch;     //e.g., existence, disease location["ovary", "ovarian"]

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

    public RuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
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
}
