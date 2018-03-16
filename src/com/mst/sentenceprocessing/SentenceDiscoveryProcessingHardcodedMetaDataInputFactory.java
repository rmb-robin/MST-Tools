package com.mst.sentenceprocessing;

import com.mst.interfaces.SentenceProcessingMetaDataInputFactory;
import com.mst.metadataProviders.DynamicRuleProvider;
import com.mst.metadataProviders.NGramsHardCodedProvider;
import com.mst.metadataProviders.PartOfSpeechHardcodedAnnotatorEntityProvider;
import com.mst.metadataProviders.RelationshipInputProviderFileImpl;
import com.mst.metadataProviders.SemanticTypeHardCodedProvider;
import com.mst.metadataProviders.VerbProcessingInputProvider;
import com.mst.model.sentenceProcessing.SentenceProcessingMetaDataInput;

public class SentenceDiscoveryProcessingHardcodedMetaDataInputFactory implements SentenceProcessingMetaDataInputFactory {

	public SentenceProcessingMetaDataInput create(){
		RelationshipInputProviderFileImpl relationshipProvider = new RelationshipInputProviderFileImpl(); 
		SentenceProcessingMetaDataInput metaDataInput = new SentenceProcessingMetaDataInput();
		metaDataInput.setNgramsInput(new NGramsHardCodedProvider().getNGrams());
		metaDataInput.setNounRelationshipsInput(relationshipProvider.getRelationships("nounrelationships.txt"));
		metaDataInput.setPartOfSpeechAnnotatorEntity(new PartOfSpeechHardcodedAnnotatorEntityProvider().getPartOfSpeechAnnotatorEntity());
		metaDataInput.setPhraseProcessingInput(new PrepositionPhraseProcessingInputFactory().create());
		metaDataInput.setSemanticTypes(new SemanticTypeHardCodedProvider().getSemanticTypes(false));
		metaDataInput.setVerbPhraseInput(new VerbPhraseInputFactoryImpl().create());
		metaDataInput.setVerbProcessingInput(new VerbProcessingInputProvider().getInput());
		metaDataInput.setPhraseRelationshipInput(relationshipProvider.getRelationships("prepphraserelations.txt"));
		metaDataInput.setDynamicEdgeCreationRules(new DynamicRuleProvider().getRules());
		return metaDataInput;
	}
	
}
