package com.mst.sentenceprocessing;

import com.mst.interfaces.SentenceProcessingMetaDataInputFactory;
import com.mst.metadataProviders.DynamicRuleProvider;
import com.mst.metadataProviders.NGramsHardCodedProvider;
import com.mst.metadataProviders.PartOfSpeechHardcodedAnnotatorEntityProvider;
import com.mst.metadataProviders.RelationshipInputProviderFileImpl;
import com.mst.metadataProviders.SemanticTypeHardCodedProvider;
import com.mst.metadataProviders.SentenceRelationshipInputProviderFileImpl;
import com.mst.metadataProviders.VerbProcessingInputProvider;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;

public class SentenceProcessingHardcodedMetaDataInputFactory implements SentenceProcessingMetaDataInputFactory {

	public SentenceProcessingMetaDataInput create(){
		
		SentenceProcessingMetaDataInput metaDataInput = new SentenceProcessingMetaDataInput();
		metaDataInput.setNgramsInput(new NGramsHardCodedProvider().getNGrams());
		metaDataInput.setNounRelationshipsInput( new SentenceRelationshipInputProviderFileImpl().getNounRelationships(7));
		metaDataInput.setPartOfSpeechAnnotatorEntity(new PartOfSpeechHardcodedAnnotatorEntityProvider().getPartOfSpeechAnnotatorEntity());
		metaDataInput.setPhraseProcessingInput(new PrepositionPhraseProcessingInputFactory().create());
		metaDataInput.setSemanticTypes(new SemanticTypeHardCodedProvider().getSemanticTypes(true));
		metaDataInput.setVerbPhraseInput(new VerbPhraseInputFactoryImpl().create());
		metaDataInput.setVerbProcessingInput(new VerbProcessingInputProvider().getInput());
		metaDataInput.setPhraseRelationshipMappings(new SentenceRelationshipInputProviderFileImpl().getPrepPhraseRelationshipMapping());
		metaDataInput.setDynamicEdgeCreationRules(new DynamicRuleProvider().getRules());
		return metaDataInput;
	}
}