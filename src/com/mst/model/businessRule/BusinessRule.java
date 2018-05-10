package com.mst.model.businessRule;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mst.jsonSerializers.ObjectIdJsonSerializer;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.List;

@Entity("businessRule")
public class BusinessRule {
    @Id
    @JsonSerialize(using=ObjectIdJsonSerializer.class)
    private ObjectId id;
    public enum LogicalOperator {AND, OR, AND_NOT, OR_NOT}
    private String organizationId;
    private String ruleType;
    private List<BusinessRule> rules;

    public ObjectId getId() {
        return id;
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

    public List<BusinessRule> getRules() {
        return rules;
    }

    public void setRules(List<BusinessRule> rules) {
        this.rules = rules;
    }
}
