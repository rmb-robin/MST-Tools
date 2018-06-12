package com.mst.model.sentenceProcessing;


import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;

@Entity("ngrams")
@Indexes({
        @Index(fields = @Field("id"))
})
public class NGramsModifierEntity {
    @Id
    private ObjectId id;
    private String originalStatement;
    private String ModifiedStatement;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getOriginalStatement() {
        return originalStatement;
    }

    public void setOriginalStatement(String originalStatement) {
        this.originalStatement = originalStatement;
    }

    public String getModifiedStatement() {
        return ModifiedStatement;
    }

    public void setModifiedStatement(String modifiedStatement) {
        ModifiedStatement = modifiedStatement;
    }
}
