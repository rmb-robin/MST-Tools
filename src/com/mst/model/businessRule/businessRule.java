package com.mst.model.businessRule;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mst.jsonSerializers.ObjectIdJsonSerializer;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.List;


enum RuleType {
    CREATE_SYNONYM { public String toString() { return "createSynonym"; } }
}

@Entity("businessRule")
public class businessRule {
    @Id
    @JsonSerialize(using=ObjectIdJsonSerializer.class)
    private ObjectId id;
    private String organizationId;
    private String ruleName;
    private RuleType ruleType;
    private String edgeName; //TODO would the edgeName be measurment or measurement:.1cm
    private String synonymousEdge; //TODO small
    private List<String> edgesToMatch;
    //TODO Because we're creating a synonym, we known that the query will use an OR
}
