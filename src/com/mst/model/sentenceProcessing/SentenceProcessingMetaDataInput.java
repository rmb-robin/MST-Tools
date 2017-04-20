package com.mst.model.sentenceProcessing;

import java.util.List;
import java.util.Map;

public class SentenceProcessingMetaDataInput {

	
	private List<NGramsModifierEntity> ngramsInput;
	private PartOfSpeechAnnotatorEntity partOfSpeechAnnotatorEntity;
	private Map<String, String> semanticTypes;
	private RelationshipInput nounRelationshipsInput;
	private VerbProcessingInput verbProcessingInput;
	private PrepositionPhraseProcessingInput phraseProcessingInput;
	private VerbPhraseInput verbPhraseInput;
	private List<PrepPhraseRelationshipMapping> phraseRelationshipMappings;
	
	
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
	public List<PrepPhraseRelationshipMapping> getPhraseRelationshipMappings() {
		return phraseRelationshipMappings;
	}
	public void setPhraseRelationshipMappings(List<PrepPhraseRelationshipMapping> phraseRelationshipMappings) {
		this.phraseRelationshipMappings = phraseRelationshipMappings;
	}
}
