package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.List;

import com.mst.interfaces.sentenceprocessing.DynamicEdgeCreationProcesser;
import com.mst.interfaces.sentenceprocessing.TokenRelationshipFactory;
import com.mst.model.sentenceProcessing.DynamicEdgeCondition;
import com.mst.model.sentenceProcessing.DynamicEdgeCreationRule;
import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

public class DynamicEdgeCreationProcesserImpl implements DynamicEdgeCreationProcesser {

	private TokenRelationshipFactory tokenRelationshipFactory;
	
	public DynamicEdgeCreationProcesserImpl() {
		tokenRelationshipFactory = new TokenRelationshipFactoryImpl();
	}
	
	//done..
	@Override
	public List<TokenRelationship> process(List<DynamicEdgeCreationRule> rules, Sentence sentence) {
		// TODO Auto-generated method stub
		List<TokenRelationship> results = new ArrayList<>();
		for(DynamicEdgeCreationRule rule: rules){
			if(isRuleValid(rule,sentence)){
				results.add(create(rule));
			}
		}
		return results;
	}
   //done..
	private boolean isRuleValid(DynamicEdgeCreationRule rule,Sentence sentence){
		for(DynamicEdgeCondition condition : rule.getConditions()){
			if(!isConditionValid(condition,sentence))return false;
		}
		return true;
	}
	
	//done...
	private boolean isConditionValid(DynamicEdgeCondition condition,Sentence sentence){
		boolean isValid = false;
		if(condition.isCondition1Token())
			isValid = isTokenConditionValid(condition,sentence);
		else
			isValid = isTokenRelationshipValid(condition);
		
		if(condition.getIsEqualTo())
			return isValid;
		return !isValid;
	}
	
	
	private boolean isTokenConditionValid(DynamicEdgeCondition condition,Sentence sentence){
		for(WordToken wordToken: sentence.getModifiedWordList()){
			if(isTokenAMatch(condition.getIsTokenSemanticType(), condition.getIsTokenPOSType(), wordToken,condition.getToken()))return true;
		}
		return false;
	}
	
	private boolean isTokenAMatch(boolean isSementicType, boolean isPos, WordToken token,String value){
		if(isSementicType){
			if(token.getSemanticType()!=null && token.getSemanticType().equals(value)) return true;
		}
		
		if(isPos){
			if(token.getPos()!=null && token.getPos().equals(value)) return true;
		}
		return token.getToken().equals(value);
		
	}
	private boolean isTokenRelationshipValid(DynamicEdgeCondition condition){
		return true;
	}
	

	private TokenRelationship create(DynamicEdgeCreationRule rule){
		return tokenRelationshipFactory.create("","", null,null);
	}

}
