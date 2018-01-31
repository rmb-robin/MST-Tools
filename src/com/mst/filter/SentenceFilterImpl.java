package com.mst.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

 
import com.mst.interfaces.filter.SentenceFilter;
import com.mst.model.SentenceQuery.EdgeMatchOnQueryResult;
import com.mst.model.SentenceQuery.EdgeQuery;
import com.mst.model.SentenceQuery.MatchInfo;
import com.mst.model.SentenceQuery.ShouldMatchOnSentenceEdgesResult;
import com.mst.model.metadataTypes.EdgeNames;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.util.TokenRelationshipUtil;

public class SentenceFilterImpl implements SentenceFilter {

	
	private boolean isEdgeNotinQuery(EdgeQuery edgeQuery, Map<String,List<TokenRelationship>> namedRelationshipsByEdgeName, Map<String,List<TokenRelationship>> notNamedRelationshipsByEdgeName){
		if(edgeQuery.getIsNamedEdge()){
			return namedRelationshipsByEdgeName.containsKey(edgeQuery.getName());
		}
		return notNamedRelationshipsByEdgeName.containsKey(edgeQuery.getName());
	}
	
	private List<TokenRelationship> getTokenRelationshipByQueryEdgeName(EdgeQuery edgeQuery, Map<String,List<TokenRelationship>> namedRelationshipsByEdgeName, Map<String,List<TokenRelationship>> notNamedRelationshipsByEdgeName){
		if(edgeQuery.getIsNamedEdge()){
			return namedRelationshipsByEdgeName.get(edgeQuery.getName());
		}
		return notNamedRelationshipsByEdgeName.get(edgeQuery.getName());
	}
	
	public EdgeMatchOnQueryResult AreEdgesMatchOnQuery(List<TokenRelationship> existingtokenRelationships,List<EdgeQuery> edgeQueries, String searchToken){	
		EdgeMatchOnQueryResult result = new EdgeMatchOnQueryResult();
		Map<String,List<TokenRelationship>> namedRelationshipsByEdgeName = TokenRelationshipUtil.getMapByEdgeName(existingtokenRelationships, true);
		Map<String,List<TokenRelationship>> notNamedRelationshipsByEdgeName = TokenRelationshipUtil.getMapByEdgeName(existingtokenRelationships,false);

		for(EdgeQuery edgeQuery: edgeQueries){
			
			if(edgeQuery.getName().equals(WordEmbeddingTypes.defaultEdge) && edgeQuery.getValues().isEmpty()){
				edgeQuery.getValues().add(searchToken);
			}
			
			HashSet<String> edgeValues = edgeQuery.getValuesLower();
			if(edgeQuery.getName().equals(EdgeNames.existence)){
				//TO DO come back..
				if(!isMatchOnExistence(notNamedRelationshipsByEdgeName, searchToken)) {
					result.setMatch(false); 
					return result;
				}
				else {
					MatchInfo info = new MatchInfo();
					info.setValue(searchToken);
					result.getMatches().put(edgeQuery.getName(),info);
					continue;
				}	
			}
			else { 
				if(!isEdgeNotinQuery(edgeQuery,namedRelationshipsByEdgeName,notNamedRelationshipsByEdgeName)) continue;
				if(edgeValues==null || edgeValues.isEmpty()) continue;
			}
			result.setDidTokenRelationsContainAnyMatches(true);
			List<String> edgeValuesList = new ArrayList<>(edgeValues);
			
			List<TokenRelationship> tokenRelationships = getTokenRelationshipByQueryEdgeName(edgeQuery,namedRelationshipsByEdgeName, notNamedRelationshipsByEdgeName);
			
			if(edgeQuery.getIsNumeric()==null)
				edgeQuery.setIsNumeric(isEdgeQueryNumeric(edgeValuesList));
			
			boolean isEdgeNumeric = edgeQuery.getIsNumeric();
			boolean isEdgeInRange =false;
			int matchCount = 0;
			for(TokenRelationship relationship: tokenRelationships){
				
				if(isEdgeNumeric && !isEdgeInRange){
					if(relationship.getFromToken().isCardinal()){
						isEdgeInRange = isNumericInRange(edgeValuesList,relationship.getFromToken().getToken());
						MatchInfo matchInfo = createMatchInfo(isEdgeInRange,"from",relationship.getFromToken().getToken());
						if(matchInfo!=null) {
							result.getMatches().put(edgeQuery.getName(),matchInfo);
							matchCount +=1;
							continue;
						}
					}
					if(relationship.getToToken().isCardinal()) 
						isEdgeInRange = isNumericInRange(edgeValuesList,relationship.getToToken().getToken());
						MatchInfo matchInfo = createMatchInfo(isEdgeInRange,"to",relationship.getToToken().getToken());
						if(matchInfo!=null) {
							{
								result.getMatches().put(edgeQuery.getName(),matchInfo);
								matchCount +=1;
							}
							
							
							}
				}
				if(!isEdgeNumeric){
					if(!edgeValues.contains(relationship.getFromToken().getToken()) && 
					   !edgeValues.contains(relationship.getToToken().getToken())) {
						continue;
					}
					else 
					{
						MatchInfo info = new MatchInfo();
						if(edgeValues.contains(relationship.getFromToken().getToken()))
						{
							info.setTokenType("from");
							info.setValue(relationship.getFromToken().getToken());
							matchCount+=1;
						}
						else 
						{
							info.setTokenType("to");
							info.setValue(relationship.getToToken().getToken());
							matchCount+=1;
						}
						result.getMatches().put(edgeQuery.getName(),info);
					}
				}
			}
			
			if(matchCount==0){
				result.setMatch(false);
				return result;
			}
			
			if(isEdgeNumeric && !isEdgeInRange){
				result.setMatch(false); 
				return result;
			}
		}
		
		result.setMatch(true);
		return result;
	}
	
	private boolean isMatchOnExistence(Map<String,List<TokenRelationship>> relationshipsByEdgeName, String searchToken){
		if(!isMatchOnEdgeName(relationshipsByEdgeName,searchToken, EdgeNames.existence))return false; 
		if(isMatchOnEdgeName(relationshipsByEdgeName, searchToken, EdgeNames.existenceMaybe))return false; 
		if(isMatchOnEdgeName(relationshipsByEdgeName, searchToken, EdgeNames.existenceNo))return false; 
		if(isMatchOnEdgeName(relationshipsByEdgeName, searchToken, EdgeNames.existencePossibility))return false; 
		return true; 
	}
	
	private boolean isMatchOnEdgeName(Map<String,List<TokenRelationship>> relationshipsByEdgeName, String searchToken, String edgeName){
		if(!relationshipsByEdgeName.containsKey(edgeName))return false;
		for(TokenRelationship relationship:relationshipsByEdgeName.get(edgeName)){
			if(relationship.isToFromTokenMatch(searchToken)) 
				return true; 
		}
		return false;
	}
	
	private MatchInfo createMatchInfo(boolean isRange, String tokenType, String value){
		if(!isRange) return null;
		MatchInfo info = new MatchInfo();
		info.setTokenType(tokenType);
		info.setValue(value); 
		return info;
	}
	

	//Numeric
	private boolean isNumericInRange(List<String> edgeValues, String relationShipValue){
		
		if(!isNumericValue(relationShipValue)) return false;
		double value = Double.parseDouble(relationShipValue);
		
		double valueOne = Double.parseDouble(edgeValues.get(0));
		double valueTwo = Double.parseDouble(edgeValues.get(1));
		double min = Math.min(valueOne, valueTwo);
		double max = Math.max(valueOne, valueTwo);
		if(value>=min && value <=max) return true;
		return false;
	}
	
	
	private boolean isEdgeQueryNumeric(List<String> edgeValues){
		if(edgeValues.size()>2) return false;
		if(!isNumericValue(edgeValues.get(0))) return false;
		if(!isNumericValue(edgeValues.get(1))) return false;
		return true;
	}

	private boolean isNumericValue(String value){
		return value.matches("[-+]?\\d*\\.?\\d+");
	}
	
	public ShouldMatchOnSentenceEdgesResult shouldAddTokenFromRelationship(TokenRelationship relation, String token){
		ShouldMatchOnSentenceEdgesResult result = new ShouldMatchOnSentenceEdgesResult();
		result.setMatch(false);
		if(relation.getFromToken()==null) return result;
		if(relation.getToToken()==null) return result;
		
		if(token.split(" ").length>1){
			token = token.replace(" ","-");
		}
		
		if(relation.getFromToken().getToken().equals(token)) {
			result.setMatch(true);
			return result;
		}
		
		if(relation.getToToken().getToken().equals(token)) {
			result.setMatch(true);
			return result;
		}
		return result;
	}

 
	
	
	
}
