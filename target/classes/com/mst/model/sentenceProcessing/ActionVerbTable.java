package com.mst.model.sentenceProcessing;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mst.jsonSerializers.UuidSerializer;

public class ActionVerbTable {

	@JsonSerialize(using=UuidSerializer.class)
	private Map<UUID,ActionVerbItem> verbyById;
	private Map<String, ActionVerbItem> verbsByWord;
	
	
	public ActionVerbTable(){
		verbyById = new HashMap<>();
		verbsByWord = new HashMap<>();
	}

	public void addValue(ActionVerbItem item){
		if(!verbsByWord.containsKey(item.getVerb()))
				verbsByWord.put(item.getVerb(),item);
		
		if(!verbyById.containsKey(item.getId()))
			verbyById.put(item.getId(), item);
	}

	public Map<UUID, ActionVerbItem> getVerbyById() {
		return verbyById;
	}

	public Map<String, ActionVerbItem> getVerbsByWord() {
		return verbsByWord;
	}

	public void setVerbyById(Map<UUID, ActionVerbItem> verbyById) {
		this.verbyById = verbyById;
	}

	public void setVerbsByWord(Map<String, ActionVerbItem> verbsByWord) {
		this.verbsByWord = verbsByWord;
	}
}


