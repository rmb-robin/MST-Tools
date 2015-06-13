package com.mst.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// The purpose for metadata, in general, is to ease downstream processing when creating the structured output (JSON that represents Ontology/Snomed mapping).
// The idea is to choose different processing paths based on features of the sentence, such as (really bad example)...
//   "Does the sentence start with a preposition and contain more than one verb?" 

public class SentenceMetadata {
	// simple - aggregate data related to the sentence as a whole
	// more simple metadata values could be added if a decision is made that storing here is preferable to computing later
	// ex1. boolean nounPhrasePresent vs. nounPhrases.size() > 1
	// ex2. boolean nounPhraseInPrepPhrase vs. for(NounPhraseMetadata npm : nounPhrases) { if(npm.isHeadEqPPObj()) return true; }
	//private boolean beginsWithPrep;
	private Map<String, Object> simple = new HashMap<String, Object>();
	
	// complex - inter and intra phrase-level data that is more involved than a simple count or boolean 
	private List<VerbPhraseMetadata> verbPhrases = new ArrayList<VerbPhraseMetadata>(); 
	private List<NounPhraseMetadata> nounPhrases = new ArrayList<NounPhraseMetadata>();
	private List<PrepPhraseMetadata> prepPhrases = new ArrayList<PrepPhraseMetadata>();
	private List<DependentPhraseMetadata> dependentPhrases = new ArrayList<DependentPhraseMetadata>();
		
	
	public int addVerbMetadata(VerbPhraseMetadata val) {
		// return the index of the phrase just added
		verbPhrases.add(val);
		return verbPhrases.size()-1;
	}
	
	public List<VerbPhraseMetadata> getVerbMetadata() { return verbPhrases; }
	
	public boolean addNounMetadata(NounPhraseMetadata val) { return nounPhrases.add(val); }
	public List<NounPhraseMetadata> getNounMetadata() { return nounPhrases; }
	
	public boolean addPrepMetadata(PrepPhraseMetadata val) { return prepPhrases.add(val); }
	public List<PrepPhraseMetadata> getPrepMetadata() { return prepPhrases; }
	
	public boolean addDependentMetadata(DependentPhraseMetadata val) { return dependentPhrases.add(val); }
	public List<DependentPhraseMetadata> getDependentMetadata() { return dependentPhrases; }
	
	//public void setBeginsWithPreposition(boolean val) { beginsWithPrep = val; };
	//public boolean beginsWithPreposition() { return beginsWithPrep; };

	public Map<String, Object> getSimpleMetadata() {
		return this.simple;
	}
	
	public Object getSimpleMetadataValue(String key) {
		return this.simple.get(key);
	}
	
	public boolean addSimpleMetadataValue(String key, Object value) {
		boolean ret = true;
		try {
			simple.put(key, value);
		} catch(Exception e) {
			ret = false;
		}
		return ret;
	}
	
	public boolean removeSimpleMetadataValue(String key) {
		boolean ret = true;
		try {
			simple.remove(key);
		} catch(Exception e) {
			ret = false;
		}
		return ret;
	}
}
