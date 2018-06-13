package com.mst.model.requests;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mst.jsonSerializers.ObjectIdJsonSerializer;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("sentenceTextRequest")
public class SentenceTextRequest extends SentenceRequestBase {
    @Id
    @JsonSerialize(using = ObjectIdJsonSerializer.class)
    private ObjectId id;
    private String text;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
