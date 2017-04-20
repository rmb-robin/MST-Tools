package com.mst.model.sentenceProcessing;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActionVerbTable {

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
}


