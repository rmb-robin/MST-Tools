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

	private class IterateRightOnTokenTokenResult{
		public int pointValue; 
		public List<RecommendedTokenRelationship> createdEdges;
	}
	
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
	
	private int getIndexOfStartRule(List<RecommendedTokenRelationship> relationships, IterationDataRule rule){
		
		
		
		
		return -1;
	}
	
	
	private List<RecommendedTokenRelationship> processLeft(List<RecommendedTokenRelationship> recommendedTokenRelationships, List<IterationDataRule> leftRules){
	
		List<RecommendedTokenRelationship> result = new ArrayList<>();
		for(IterationDataRule rule: leftRules){
			String startRelationship = rule.getStartRelationship();
	
			List<Integer> verbMinusOneIndexes = getStartIndexes(recommendedTokenRelationships,startRelationship, true);
			
			for(int index: verbMinusOneIndexes){
				RecommendedTokenRelationship relationship = iterateLeft(recommendedTokenRelationships, leftRules, index);
				if(relationship!=null) result.add(relationship);
			}
		}
		return result;
	}
	
	private List<RecommendedTokenRelationship> processRight(List<RecommendedTokenRelationship> recommendedTokenRelationships, List<IterationDataRule> rightRules){
		
		List<Integer> verbMinusOneIndexes = getStartIndexes(recommendedTokenRelationships,false);
		List<RecommendedTokenRelationship> result = new ArrayList<>();
		for(int index : verbMinusOneIndexes){
			List<RecommendedTokenRelationship> relationships = iterateRight(recommendedTokenRelationships, rightRules, index);
			if(relationships!=null)
				result.addAll(relationships);
			
		}
		return result;
	
	}
		
	private RecommendedTokenRelationship iterateLeft(List<RecommendedTokenRelationship> recommendedTokenRelationships, List<IterationDataRule> leftRules, int verbMinusOneIndex){
			
			IterationDataRule foundRule = null;
			RecommendedTokenRelationship matchedRelationship = null;
			Map<String,IterationDataRule> rulesByStopEdgeName = getRulesByStopEdgeName(leftRules);
			for(int i = verbMinusOneIndex; i>=0; i--){
				if(i==verbMinusOneIndex) continue; 
				
				RecommendedTokenRelationship relationship = recommendedTokenRelationships.get(i);
				IterationDataRule matchedRule = getIterationRuleForRelationship(rulesByStopEdgeName, relationship);
				if(matchedRule==null)continue; 
				
				if(isOverridingExistingMatchedRule(foundRule,matchedRule)){
					foundRule = matchedRule;
					matchedRelationship = relationship;
				}
				
			}
			
			if(foundRule==null)return null;
			return createSubjectEdge(matchedRelationship,  recommendedTokenRelationships.get(verbMinusOneIndex));
	}

	
	private List<RecommendedTokenRelationship> iterateRight(List<RecommendedTokenRelationship> recommendedTokenRelationships, List<IterationDataRule> rightRules, int verbPlusOneIndex){
		
		IterationDataRule foundRule = null;
		RecommendedTokenRelationship matchedRelationship = null;
		Map<String,IterationDataRule> rulesByStopEdgeName = getRulesByStopEdgeName(rightRules);

		IterationDataRule tokenTokenRule = null;
		if(rulesByStopEdgeName.containsKey(WordEmbeddingTypes.defaultEdge)){
			tokenTokenRule = rulesByStopEdgeName.get(WordEmbeddingTypes.defaultEdge);
			rulesByStopEdgeName.remove(WordEmbeddingTypes.defaultEdge);
		}
		
		
		for(int i = verbPlusOneIndex; i<recommendedTokenRelationships.size(); i++){
			if(i==verbPlusOneIndex) continue; 
			
			RecommendedTokenRelationship relationship = recommendedTokenRelationships.get(i);
			IterationDataRule matchedRule = getIterationRuleForRelationship(rulesByStopEdgeName, relationship);
			if(matchedRule==null)continue; 
			
			if(isOverridingExistingMatchedRule(foundRule,matchedRule)){
				foundRule = matchedRule;
				matchedRelationship = relationship;
			}
		}
		
		
		if(tokenTokenRule!=null){
			IterateRightOnTokenTokenResult  result = iterateRightOnTokenToken(verbPlusOneIndex, recommendedTokenRelationships, tokenTokenRule);
			if(result.pointValue > foundRule.getPointValue())
				return result.createdEdges;
		}
		
		if(foundRule==null)return null;
		RecommendedTokenRelationship createdRelationship = createSubjectComplimentEdge(recommendedTokenRelationships.get(verbPlusOneIndex), matchedRelationship);
		List<RecommendedTokenRelationship> result = new ArrayList<>();
		result.add(createdRelationship);
		return result;
	}


	
	private IterateRightOnTokenTokenResult iterateRightOnTokenToken(int verbPlusOneIndex, List<RecommendedTokenRelationship> relationships, IterationDataRule tokenTokenRule){
		IterateRightOnTokenTokenResult result = new IterateRightOnTokenTokenResult();
		result.createdEdges = new ArrayList<>();
		for(int i = verbPlusOneIndex; i<relationships.size(); i++){
			if(i==verbPlusOneIndex) continue;
		
			RecommendedTokenRelationship relationship = relationships.get(i);
			if(!relationship.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.defaultEdge))continue;
			if(relationship.getTokenRelationship().getToToken().getPropertyValueType().equals(tokenTokenRule.getPropertyValueType())){
				result.pointValue = tokenTokenRule.getPointValue();
				result.createdEdges.add(
						createSubjectComplimentEdge(relationships.get(verbPlusOneIndex), relationship));

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
	
	private boolean isOverridingExistingMatchedRule(IterationDataRule foundRule, IterationDataRule matchedRule){
		//need to abstract... 
		if(foundRule== null){
			return true;
		}

		if(foundRule.getPointValue() < matchedRule.getPointValue())
			return true;
		
		return false;
	}
	
	private IterationDataRule getIterationRuleForRelationship(Map<String, IterationDataRule> rules, RecommendedTokenRelationship recommendedTokenRelationship){
		String key = recommendedTokenRelationship.getTokenRelationship().getEdgeName();
		if(!rules.containsKey(key))return null; 
		return rules.get(key);
	}
	
	
	
	private Map<String,IterationDataRule> getRulesByStopEdgeName(List<IterationDataRule> rules){
		Map<String, IterationDataRule> result= new HashMap<>();
		
		for(IterationDataRule rule: rules){
			if(!result.containsKey(rule.getEdgeNameTolookfor())){
				result.put(rule.getEdgeNameTolookfor(),rule);
			}
		}

		return result;
		
	}
		
	private List<Integer> getStartIndexes(List<RecommendedTokenRelationship> recommendedTokenRelationships,String startEdgeName, boolean isLeft){
		
		List<Integer> result = new ArrayList<>();
		
		String edgeName = startEdgeName;
		
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
