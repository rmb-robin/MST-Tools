package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mst.interfaces.sentenceprocessing.DistinctTokenRelationshipDeterminer;
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
	private DistinctTokenRelationshipDeterminer distinctDeterminer = new DistinctTokenRelationshipDeterminerImpl();
	
	public IterationRuleProcesser(){
		tokenRelationshipFactory = new TokenRelationshipFactoryImpl();
	}
	
	
	public List<RecommendedTokenRelationship> process(List<RecommendedTokenRelationship> recommendedTokenRelationships, IterationRuleProcesserInput input){
		List<RecommendedTokenRelationship> result = new ArrayList<>();
		result.addAll(processLeft(recommendedTokenRelationships, input.getLeftRules()));
		result.addAll(processRight(recommendedTokenRelationships, input.getRightRules()));
		return distinctDeterminer.getDistinctRecommendedRelationships(result);
	}
	
	private List<RecommendedTokenRelationship> processLeft(List<RecommendedTokenRelationship> recommendedTokenRelationships, List<IterationDataRule> leftRules){
		Map<Integer, RecommendedTokenRelationship> result = new HashMap<>();
		for(IterationDataRule rule: leftRules){
			Map<Integer, RecommendedTokenRelationship> relationships = iterateLeft(recommendedTokenRelationships, rule);
			if(relationships.isEmpty()) continue; 
		
			updateMaxRuleMap(relationships, result);
		}
		return new ArrayList<>(result.values());
		
	}
	
	private void updateMaxRuleMap(Map<Integer, RecommendedTokenRelationship> relationships, Map<Integer, RecommendedTokenRelationship> allRelationships){	
		for(Entry<Integer, RecommendedTokenRelationship> entry: relationships.entrySet()){
			allRelationships.put(entry.getKey(),entry.getValue());
		}
	}

	
	
	private Map<Integer, RecommendedTokenRelationship> iterateLeft(List<RecommendedTokenRelationship> recommendedTokenRelationships, IterationDataRule rule){
		
		List<Integer> indexes = getIndexes(recommendedTokenRelationships,rule.getStartRelationship(), true);
		Map<Integer,RecommendedTokenRelationship> result = new HashMap<>();

		if(rule.shouldUseSameEdge()){
			for(int index: indexes) {
				result.put(index, createSubjectEdge(recommendedTokenRelationships.get(index),  recommendedTokenRelationships.get(index),rule));
			}
			return result;
			
		}

		for(int index: indexes) {
			for(int i = index; i>=0; i--){
				
				if(index==0) continue;
				if(i==index) continue; 
				RecommendedTokenRelationship relationship = recommendedTokenRelationships.get(i);
				if(shouldBreakIteration(rule,relationship)) break;
				
				boolean isMatch = isIterationRuleForRelationshipMatch(rule, relationship);
				if(isMatch)
					result.put(index, createSubjectEdge(relationship,  recommendedTokenRelationships.get(index),rule));
			}	
		}
		return result;
	}

	private List<RecommendedTokenRelationship> processRight(List<RecommendedTokenRelationship> recommendedTokenRelationships, List<IterationDataRule> rightRules){
		
		Map<Integer,RecommendedTokenRelationship> result = new HashMap<>();
		for(IterationDataRule rule: rightRules){
			Map<Integer,RecommendedTokenRelationship> relationships = iterateRight(recommendedTokenRelationships, rule);
			if(relationships.isEmpty()) continue; 
		
			updateMaxRuleMap(relationships, result);
		}
		return new ArrayList<>(result.values());
	}

	
	private Map<Integer,RecommendedTokenRelationship> iterateRight(List<RecommendedTokenRelationship> recommendedTokenRelationships, IterationDataRule rule){	
		List<Integer> indexes = getIndexes(recommendedTokenRelationships,rule.getStartRelationship(), true);
		
		if(rule.shouldUseSameEdge()){
			Map<Integer,RecommendedTokenRelationship> result = new HashMap<>();
			
			for(int index: indexes) {
				result.put(index,createSubjectComplimentEdge(recommendedTokenRelationships.get(index),  recommendedTokenRelationships.get(index),rule));
			}
			return result;
		}
		
		if(!rule.getEdgeNameTolookfor().equals(WordEmbeddingTypes.defaultEdge)) 
			return iterateRightNonTokenToken(recommendedTokenRelationships, indexes, rule);
		return iterateRightTokenToken(recommendedTokenRelationships, indexes, rule);
		
	}

	
	private Map<Integer,RecommendedTokenRelationship> iterateRightNonTokenToken(List<RecommendedTokenRelationship> recommendedTokenRelationships,List<Integer> indexes,IterationDataRule rule){
		Map<Integer,RecommendedTokenRelationship> result = new HashMap<>();
		for(int index: indexes) {
			for(int i = index; i<recommendedTokenRelationships.size(); i++){
				if(i==index) continue; 
				
				RecommendedTokenRelationship relationship = recommendedTokenRelationships.get(i);
				if(shouldBreakIteration(rule,relationship)){
					break; 
				}
				boolean isMatch = isIterationRuleForRelationshipMatch(rule, relationship);
				if(isMatch)
					result.put(index,createSubjectComplimentEdge(recommendedTokenRelationships.get(index),relationship,rule));
			}
			
		}
		return result;
	}
	
	
	
	private Map<Integer,RecommendedTokenRelationship> iterateRightTokenToken(List<RecommendedTokenRelationship> recommendedTokenRelationships,List<Integer> indexes,IterationDataRule rule){

		Map<Integer,RecommendedTokenRelationship> result = new HashMap<>();
		for(int index: indexes){
			for(int i = index; i<recommendedTokenRelationships.size(); i++){
				if(i==index) continue;
			
				RecommendedTokenRelationship relationship = recommendedTokenRelationships.get(i);
				if(shouldBreakIteration(rule,relationship)){
					break; 
				}
				if(!relationship.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.defaultEdge))continue;
				if(relationship.getTokenRelationship().getToToken().getPropertyValueType().equals(rule.getPropertyValueType())){
					
					result.put(index,
							createSubjectComplimentEdge(recommendedTokenRelationships.get(index), relationship, rule));
				}
			}
		}
		return result;
	}
	
	
	

	
	
	private RecommendedTokenRelationship createSubjectEdge(RecommendedTokenRelationship startEdge, RecommendedTokenRelationship endEdge,IterationDataRule rule){
		RecommendedTokenRelationship relationship =  tokenRelationshipFactory.createRecommendedRelationship(WordEmbeddingTypes.subjectVerb, EdgeTypes.related, startEdge.getTokenRelationship().getFromToken(), endEdge.getTokenRelationship().getToToken(), 
				this.getClass().getName());	
		relationship.setIterationPoint(rule.getPointValue());
		return relationship;
	}
	
	private RecommendedTokenRelationship createSubjectComplimentEdge(RecommendedTokenRelationship startEdge, RecommendedTokenRelationship endEdge, IterationDataRule rule){

		RecommendedTokenRelationship relationship =  tokenRelationshipFactory.createRecommendedRelationship(WordEmbeddingTypes.subjectComplementVerb, EdgeTypes.related, startEdge.getTokenRelationship().getFromToken(), endEdge.getTokenRelationship().getToToken(), 
				this.getClass().getName());
		relationship.setIterationPoint(rule.getPointValue());
		return relationship;
	}
	

	private boolean shouldBreakIteration(IterationDataRule rule, RecommendedTokenRelationship recommendedTokenRelationship){
		if(rule.getEdgeNameToStopfor()==null) return false; 
		return rule.getEdgeNameToStopfor().equals(recommendedTokenRelationship.getTokenRelationship().getEdgeName());
	}
	
	private boolean isIterationRuleForRelationshipMatch(IterationDataRule rule, RecommendedTokenRelationship recommendedTokenRelationship){
		return rule.getEdgeNameTolookfor().equals(recommendedTokenRelationship.getTokenRelationship().getEdgeName());
	}
	
		
	private List<Integer> getIndexes(List<RecommendedTokenRelationship> recommendedTokenRelationships,String edgeName, boolean isLeft){
		
		List<Integer> result = new ArrayList<>();
	
		for(int i =0;i<recommendedTokenRelationships.size();i++){
			
			RecommendedTokenRelationship relationship = recommendedTokenRelationships.get(i);
			if(relationship.getTokenRelationship().getEdgeName().equals(edgeName)){
				result.add(i);
				
			}
		}
		return result;
	}
	
	
	
	
}
