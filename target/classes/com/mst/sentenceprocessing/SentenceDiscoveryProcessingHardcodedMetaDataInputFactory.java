package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.List;

import com.mst.interfaces.SentenceProcessingMetaDataInputFactory;
import com.mst.metadataProviders.DynamicRuleProvider;
import com.mst.metadataProviders.NGramsHardCodedProvider;
import com.mst.metadataProviders.PartOfSpeechHardcodedAnnotatorEntityProvider;
import com.mst.metadataProviders.RelationshipInputProviderFileImpl;
import com.mst.metadataProviders.SemanticTypeHardCodedProvider;
import com.mst.metadataProviders.VerbProcessingInputProvider;
import com.mst.model.metadataTypes.PropertyValueTypes;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.sentenceProcessing.IterationDataRule;
import com.mst.model.sentenceProcessing.IterationRuleProcesserInput;
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
		metaDataInput.setIterationRuleProcesserInput(createIterationRuleProcessorInput());
		return metaDataInput;
	}
	
	public IterationRuleProcesserInput createIterationRuleProcessorInput(){
		
		IterationRuleProcesserInput input = new IterationRuleProcesserInput();
		input.setLeftRules(getLeftRules());
		input.setRightRules(getRightRules());
		return input; 
	}
	
	private List<IterationDataRule> getLeftRules(){
		List<IterationDataRule> rules = new ArrayList<>();
	
		rules.add(createRuleWithSameStartEnd(WordEmbeddingTypes.verbMinus, WordEmbeddingTypes.verbMinus, 10));
		rules.add(createRuleWithStop(WordEmbeddingTypes.verbMinus, WordEmbeddingTypes.verbPlus, 20, WordEmbeddingTypes.tokenToken));
		rules.add(createRuleWithStop(WordEmbeddingTypes.verbMinus, WordEmbeddingTypes.prepMinus, 30, WordEmbeddingTypes.verbPlus));
		return rules;
	}
//	
	private List<IterationDataRule> getRightRules(){
		List<IterationDataRule> rules = new ArrayList<>();
		rules.add(createRule(WordEmbeddingTypes.verbPrep, WordEmbeddingTypes.prepPlus, 5));	
		rules.add(createRule(WordEmbeddingTypes.verbPrep, WordEmbeddingTypes.tokenToken, 10, PropertyValueTypes.NounPhraseEnd));
		rules.add(createRuleWithSameStartEnd(WordEmbeddingTypes.verbPlus, WordEmbeddingTypes.verbPlus, 20));
		rules.add(createRule(WordEmbeddingTypes.verbPlus, WordEmbeddingTypes.tokenToken, 30, PropertyValueTypes.NounPhraseEnd,WordEmbeddingTypes.verbMinus));
		//rules.add(createRule(WordEmbeddingTypes.firstVerb, WordEmbeddingTypes.secondVerb, 40));
		return rules;
	}
	
//	private List<IterationDataRule> getRightRules(){
//		List<IterationDataRule> rules = new ArrayList<>();
//		rules.add(createRule(WordEmbeddingTypes.verbPrep, WordEmbeddingTypes.firstPrep, 5));	
//		rules.add(createRule(WordEmbeddingTypes.verbPrep, WordEmbeddingTypes.defaultEdge, 10, PropertyValueTypes.NounPhraseEnd));
//		rules.add(createRule(WordEmbeddingTypes.firstVerb, WordEmbeddingTypes.firstVerb, 20));
//		rules.add(createRule(WordEmbeddingTypes.firstVerb, WordEmbeddingTypes.defaultEdge, 30, PropertyValueTypes.NounPhraseEnd,WordEmbeddingTypes.secondVerb));
//		//rules.add(createRule(WordEmbeddingTypes.firstVerb, WordEmbeddingTypes.secondPrep, 40));
//		rules.add(createRuleWithStop(WordEmbeddingTypes.firstVerb, WordEmbeddingTypes.secondPrep, 40,WordEmbeddingTypes.secondVerb));
//		
//		return rules;
//	}
	
	private IterationDataRule createRule(String startEdge, String endEdge,int point){
		IterationDataRule rule = new IterationDataRule();
		rule.setStartRelationship(startEdge);
		rule.setEdgeNameTolookfor(endEdge);
		rule.setPointValue(point);
		return rule;
	}
	

	private IterationDataRule createRule(String startEdge, String endEdge,int point, PropertyValueTypes propertyValueTypes){
		IterationDataRule rule = createRule(startEdge, endEdge, point);
		rule.setPropertyValueType(propertyValueTypes);
		return rule;
	}

	private IterationDataRule createRule(String startEdge, String endEdge,int point, PropertyValueTypes propertyValueTypes,String stopEdge){
		IterationDataRule rule = createRule(startEdge, endEdge, point);
		rule.setPropertyValueType(propertyValueTypes);
		rule.setEdgeNameToStopfor(stopEdge);
		return rule;
	}
	
	
	private IterationDataRule createRuleWithSameStartEnd(String startEdge, String endEdge,int point){
		IterationDataRule rule = createRule(startEdge, endEdge, point);
		rule.setUseSameEdgeName(true);
		return rule;
	}
	
	private IterationDataRule createRuleWithStop(String startEdge, String endEdge,int point,String stopEdge){
		IterationDataRule rule = createRule(startEdge, endEdge, point);
		rule.setEdgeNameToStopfor(stopEdge);
		return rule;
	}
	
	
	
}
