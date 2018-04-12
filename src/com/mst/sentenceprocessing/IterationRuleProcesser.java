package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mst.interfaces.sentenceprocessing.TokenRelationshipFactory;
import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.metadataTypes.EdgeTypes;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.sentenceProcessing.IterationDataRule;
import com.mst.model.sentenceProcessing.IterationRuleProcesserInput;
import com.mst.model.sentenceProcessing.TokenRelationship;

public class IterationRuleProcesser {


	
	private TokenRelationshipFactory tokenRelationshipFactory;
	
	public IterationRuleProcesser(){
		tokenRelationshipFactory = new TokenRelationshipFactoryImpl();
	}
	
	
	public List<RecommendedTokenRelationship> process(List<RecommendedTokenRelationship> recommendedTokenRelationships, IterationRuleProcesserInput input){
		List<RecommendedTokenRelationship> result = new ArrayList<>();
		result.addAll(processLeft(recommendedTokenRelationships, input.getLeftRules()));
		result.addAll(processRight(recommendedTokenRelationships, input.getRightRules()));
		return result;
	}
	
	private List<RecommendedTokenRelationship> processLeft(List<RecommendedTokenRelationship> recommendedTokenRelationships, List<IterationDataRule> leftRules){
		List<RecommendedTokenRelationship> result = new ArrayList<>();
		int maxRuleScore = 0; 
		for(IterationDataRule rule: leftRules){
			List<RecommendedTokenRelationship> relationships = iterateLeft(recommendedTokenRelationships, rule);
			if(relationships.isEmpty()) continue; 
		
			if(rule.getPointValue()> maxRuleScore) 
				result = relationships;
		}
		return result;
		
	}
	
	private List<RecommendedTokenRelationship> iterateLeft(List<RecommendedTokenRelationship> recommendedTokenRelationships, IterationDataRule rule){
		
		List<Integer> indexes = getIndexes(recommendedTokenRelationships,rule.getStartRelationship(), true);
		List<RecommendedTokenRelationship> result = new ArrayList<>();
		
		
		for(int index: indexes) {
			for(int i = index; i>=0; i--){
				if(i==index) continue; 
				
				RecommendedTokenRelationship relationship = recommendedTokenRelationships.get(i);
				boolean isMatch = isIterationRuleForRelationshipMatch(rule, relationship);
				if(isMatch)
					result.add(createSubjectEdge(relationship,  recommendedTokenRelationships.get(index)));
			}	
		}
		return result;
}
	
	
	
	private List<RecommendedTokenRelationship> processRight(List<RecommendedTokenRelationship> recommendedTokenRelationships, List<IterationDataRule> rightRules){
		
		List<RecommendedTokenRelationship> result = new ArrayList<>();
		int maxRuleScore = 0; 
		for(IterationDataRule rule: rightRules){
			List<RecommendedTokenRelationship> relationships = iterateRight(recommendedTokenRelationships, rule);
			if(relationships.isEmpty()) continue; 
		
			if(rule.getPointValue()> maxRuleScore) 
				result = relationships;
		}
		return result;
	
	}

	
	private List<RecommendedTokenRelationship> iterateRight(List<RecommendedTokenRelationship> recommendedTokenRelationships, IterationDataRule rule){	
		List<Integer> indexes = getIndexes(recommendedTokenRelationships,rule.getStartRelationship(), true);
		if(!rule.getEdgeNameTolookfor().equals(WordEmbeddingTypes.defaultEdge)) 
			return iterateRightNonTokenToken(recommendedTokenRelationships, indexes, rule);
		return iterateRightTokenToken(recommendedTokenRelationships, indexes, rule);
		
	}

	
	private List<RecommendedTokenRelationship> iterateRightNonTokenToken(List<RecommendedTokenRelationship> recommendedTokenRelationships,List<Integer> indexes,IterationDataRule rule){
		List<RecommendedTokenRelationship> result = new ArrayList<>();
		for(int index: indexes) {
			for(int i = index; i<recommendedTokenRelationships.size(); i++){
				if(i==index) continue; 
				
				RecommendedTokenRelationship relationship = recommendedTokenRelationships.get(i);
				boolean isMatch = isIterationRuleForRelationshipMatch(rule, relationship);
				if(isMatch)
					result.add(createSubjectComplimentEdge(recommendedTokenRelationships.get(index),relationship));
			}	
		}
		return result;
	}
	
	
	
	private List<RecommendedTokenRelationship> iterateRightTokenToken(List<RecommendedTokenRelationship> recommendedTokenRelationships,List<Integer> indexes,IterationDataRule rule){

		List<RecommendedTokenRelationship> result = new ArrayList<>();
		for(int index: indexes){
			for(int i = index; i<recommendedTokenRelationships.size(); i++){
				if(i==index) continue;
			
				RecommendedTokenRelationship relationship = recommendedTokenRelationships.get(i);
				if(!relationship.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.defaultEdge))continue;
				if(relationship.getTokenRelationship().getToToken().getPropertyValueType().equals(rule.getPropertyValueType())){
					
					result.add(
							createSubjectComplimentEdge(recommendedTokenRelationships.get(index), relationship));
				}
			}
		}
		return result;
	}
	
	
	

	
	
	private RecommendedTokenRelationship createSubjectEdge(RecommendedTokenRelationship startEdge, RecommendedTokenRelationship endEdge){

		//currently 
		return tokenRelationshipFactory.createRecommendedRelationship(WordEmbeddingTypes.subjectVerb, EdgeTypes.related, startEdge.getTokenRelationship().getFromToken(), endEdge.getTokenRelationship().getToToken(), 
				this.getClass().getName());
		
		
	}
	
	private RecommendedTokenRelationship createSubjectComplimentEdge(RecommendedTokenRelationship startEdge, RecommendedTokenRelationship endEdge){

		//currently 
		return tokenRelationshipFactory.createRecommendedRelationship(WordEmbeddingTypes.subjectComplementVerb, EdgeTypes.related, startEdge.getTokenRelationship().getFromToken(), endEdge.getTokenRelationship().getToToken(), 
				this.getClass().getName());
	}
	

	private boolean isIterationRuleForRelationshipMatch(IterationDataRule rule, RecommendedTokenRelationship recommendedTokenRelationship){
		return rule.getEdgeNameTolookfor().equals(recommendedTokenRelationship.getTokenRelationship().getEdgeName());
	}
	
		
	private List<Integer> getIndexes(List<RecommendedTokenRelationship> recommendedTokenRelationships,String edgeName, boolean isLeft){
		
		List<Integer> result = new ArrayList<>();
	
		for(int i =0;i<recommendedTokenRelationships.size();i++){
			
			RecommendedTokenRelationship relationship = recommendedTokenRelationships.get(i);
			if(relationship.getTokenRelationship().getEdgeName().equals(edgeName)){
				
				if(i==0){
					
					if(!isLeft)
						result.add(i);
				}
				else {
					result.add(i);
				}
			}
		}
		return result;
	}
	
	
	
	
}
