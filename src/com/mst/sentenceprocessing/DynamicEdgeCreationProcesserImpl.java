package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.activemq.protobuf.compiler.parser.Token;

import com.mst.interfaces.sentenceprocessing.DynamicEdgeCreationProcesser;
import com.mst.interfaces.sentenceprocessing.TokenRelationshipFactory;
import com.mst.model.metadataTypes.EdgeTypes;
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
	

	public List<TokenRelationship> process(List<DynamicEdgeCreationRule> rules, Sentence sentence) {
		// TODO Auto-generated method stub
		List<TokenRelationship> results = new ArrayList<>();
		for(DynamicEdgeCreationRule rule: rules){
			if(isRuleValid(rule,sentence)){
				TokenRelationship relationship = create(rule,sentence); 
				if(relationship!=null)
					results.add(relationship);
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
			isValid = isTokenRelationshipValid(condition,sentence);
		
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

	private boolean isTokenRelationshipValid(DynamicEdgeCondition condition,Sentence sentence){
		 if(condition.getEdgeNames().isEmpty()) return true;
		 Map<String,List<TokenRelationship>> map = sentence.getTokenRelationsByNameMap();
		 for(String edgeName: condition.getEdgeNames()){
			 if(map.containsKey(edgeName)){
				 if(isTokenRelationshipmatch(map.get(edgeName),condition))return true;
			 }
		 }
		 return false;
		 
	}
	
	
	private boolean areTokensMatch (boolean isSementicType, boolean isPos, WordToken token,List<String> values){
		for(String value: values){
			if(isTokenAMatch(isSementicType, isPos, token, value)) return true;
		}
		return false;
	}
	
	private boolean isTokenRelationshipmatch(List<TokenRelationship> tokenRelationships, DynamicEdgeCondition condition){
		if(condition.getToTokens().isEmpty() && condition.getFromTokens().isEmpty()) return true;
		
		for(TokenRelationship tokenRelationship: tokenRelationships){
			if(!condition.getFromTokens().isEmpty()){
				if(areTokensMatch(condition.getIsFromTokenSemanticType(), condition.getIsFromTokenPOSType(), tokenRelationship.getFromToken(),condition.getFromTokens()))
					return true;
			}

			if(!condition.getToTokens().isEmpty()){
				if(areTokensMatch(condition.getIsToTokenSemanticType(), condition.getIsToTokenPOSType(), tokenRelationship.getToToken(),condition.getToTokens()))
					return true;
			}
		}
		return false;
	}

	private TokenRelationship create(DynamicEdgeCreationRule rule, Sentence sentence){
		WordToken from=null; 
		WordToken to=null;
		if(!rule.getFromEdgeNames().isEmpty()){
			from = getFromTokenFromEdgesNames(rule.getFromEdgeNames(),sentence);
			if(from==null)return null;
		}
	
		for(WordToken token: sentence.getModifiedWordList()){
			if(from==null && isTokenAMatch(rule.isFromTokenSementicType(),false, token, rule.getFromToken())){
				from = token;
				continue;
			}
			if(to==null && isTokenAMatch(rule.isToTokenSementicType(), false, token,rule.getToToken()))
				to = token;
		}
		
		if(from==null) return null;
		if(to==null)return null;
		
		return tokenRelationshipFactory.create(rule.getEdgeName(),EdgeTypes.related, from,to);
	}

	private WordToken getFromTokenFromEdgesNames(List<String> edgeNames, Sentence sentence){
		Map<String,List<TokenRelationship>> map = sentence.getTokenRelationsByNameMap();
		for(String edgeName:edgeNames){
			if(map.containsKey(edgeName)){
				return map.get(edgeName).get(0).getFromToken();
			}
		}
		return null;
	}
	
}
