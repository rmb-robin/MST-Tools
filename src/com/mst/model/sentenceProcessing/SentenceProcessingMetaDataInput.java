package com.mst.model.sentenceProcessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("sentenceprocessingmetadatainput")
public class SentenceProcessingMetaDataInput {

	
	@Id
	private ObjectId id;
	private List<NGramsModifierEntity> ngramsInput;
	private PartOfSpeechAnnotatorEntity partOfSpeechAnnotatorEntity;
	private Map<String, String> semanticTypes;  
	private VerbProcessingInput verbProcessingInput;
	private VerbPhraseInput verbPhraseInput;
	private List<DynamicEdgeCreationRule> dynamicEdgeCreationRules;
	private PrepositionPhraseProcessingInput phraseProcessingInput;	
	
	private RelationshipInput nounRelationshipsInput;   //THis should be the standard.. 
	private RelationshipInput phraseRelationshipInput; 
	private List<PrepPhraseRelationshipMapping> phraseRelationshipMappings;
	private boolean isSentenceProcessing; 
	
	
	public List<NGramsModifierEntity> getNgramsInput() {
		return ngramsInput;
	}
	public void setNgramsInput(List<NGramsModifierEntity> ngramsInput) {
		this.ngramsInput = ngramsInput;
	}
	public PartOfSpeechAnnotatorEntity getPartOfSpeechAnnotatorEntity() {
		return partOfSpeechAnnotatorEntity;
	}
	public void setPartOfSpeechAnnotatorEntity(PartOfSpeechAnnotatorEntity partOfSpeechAnnotatorEntity) {
		this.partOfSpeechAnnotatorEntity = partOfSpeechAnnotatorEntity;
	}
	public Map<String, String> getSemanticTypes() {
		return semanticTypes;
	}
	public void setSemanticTypes(Map<String, String> semanticTypes) {
		this.semanticTypes = semanticTypes;
	}
	public RelationshipInput getNounRelationshipsInput() {
		return nounRelationshipsInput;
	}
	public void setNounRelationshipsInput(RelationshipInput nounRelationshipsInput) {
		this.nounRelationshipsInput = nounRelationshipsInput;
	}
	public VerbProcessingInput getVerbProcessingInput() {
		return verbProcessingInput;
	}
	public void setVerbProcessingInput(VerbProcessingInput verbProcessingInput) {
		this.verbProcessingInput = verbProcessingInput;
	}
	public PrepositionPhraseProcessingInput getPhraseProcessingInput() {
		return phraseProcessingInput;
	}
	public void setPhraseProcessingInput(PrepositionPhraseProcessingInput phraseProcessingInput) {
		this.phraseProcessingInput = phraseProcessingInput;
	}
	public VerbPhraseInput getVerbPhraseInput() {
		return verbPhraseInput;
	}
	public void setVerbPhraseInput(VerbPhraseInput verbPhraseInput) {
		this.verbPhraseInput = verbPhraseInput;
	}
	
	
	
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public List<DynamicEdgeCreationRule> getDynamicEdgeCreationRules() {
		return dynamicEdgeCreationRules;
	}
	public void setDynamicEdgeCreationRules(List<DynamicEdgeCreationRule> dynamicEdgeCreationRules) {
		this.dynamicEdgeCreationRules = dynamicEdgeCreationRules;
	}
	
	public Map<String, List<String>> getSemanticTypesByTypeName(){
		if(this.getSemanticTypes()==null) return new HashMap<String,List<String>>();
		Map<String,List<String>> result = new HashMap<String,List<String>>();
		
		for(Entry<String,String> entry: this.getSemanticTypes().entrySet()){
			if(!result.containsKey(entry.getValue()))
					result.put(entry.getValue(),new ArrayList<String>());
			
			result.get(entry.getValue()).add(entry.getKey());
		}
		return result;
		
	}
	public RelationshipInput getPhraseRelationshipInput() {
		return phraseRelationshipInput;
	}
	public void setPhraseRelationshipInput(RelationshipInput phraseRelationshipInput) {
		this.phraseRelationshipInput = phraseRelationshipInput;
	}
	public List<PrepPhraseRelationshipMapping> getPhraseRelationshipMappings() {
		return phraseRelationshipMappings;
	}
	public void setPhraseRelationshipMappings(List<PrepPhraseRelationshipMapping> phraseRelationshipMappings) {
		this.phraseRelationshipMappings = phraseRelationshipMappings;
	}
	public boolean getIsSentenceProcessing() {
		return isSentenceProcessing;
	}
	public void setIsSentenceProcessing(boolean isSentenceProcessing) {
		this.isSentenceProcessing = isSentenceProcessing;
	}
}
