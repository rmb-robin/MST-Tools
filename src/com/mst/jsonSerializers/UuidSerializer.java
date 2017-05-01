package com.mst.jsonSerializers;

import java.io.IOException;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class UuidSerializer extends JsonSerializer<UUID>{
	
	@Override
    public void serialize(UUID uuid, JsonGenerator j, SerializerProvider s) throws IOException, JsonProcessingException {
		if(uuid == null) {
            j.writeNull();
        } else {
            j.writeString(uuid.toString());
        }
    }
}


