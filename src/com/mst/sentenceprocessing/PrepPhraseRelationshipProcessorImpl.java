package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mst.interfaces.PrepPhraseRelationshipProcessor;
import com.mst.interfaces.RelationshipProcessor;
import com.mst.model.WordToken;
import com.mst.model.gentwo.PartOfSpeachTypes;
import com.mst.model.gentwo.PrepPhraseRelationshipMapping;
import com.mst.model.gentwo.PropertyValueTypes;
import com.mst.model.gentwo.RelationshipInput;
import com.mst.model.gentwo.RelationshipMapping;
import com.mst.model.gentwo.TokenRelationship;


public class PrepPhraseRelationshipProcessorImpl extends RelationshipProcessorBase implements PrepPhraseRelationshipProcessor { 
	
	private Map<String, List<PrepPhraseRelationshipMapping>> relationshipMap; 
	private Map<String, List<PrepPhraseRelationshipMapping>> semanticTypeRelationshipMap;
	private String modifierFrameName = "f_modifier";
	private String relatedFrameName = "f_related";
	
	public List<TokenRelationship> process(List<WordToken> tokens, List<PrepPhraseRelationshipMapping> prepPhraseRelationshipMappings) {
		this.wordTokens = tokens;
		setrelationshipMaps(prepPhraseRelationshipMappings);
		return createRelationships();
	}
	
	private List<TokenRelationship> createRelationships(){
		List<TokenRelationship> result = new ArrayList<>();
		
		for(int i =0;i<wordTokens.size();i++){
			WordToken wordToken = wordTokens.get(i);
			if(wordToken.getPos()!=PartOfSpeachTypes.IN) continue;
			List<TokenRelationship> tokenRelationships = getTokenRelationship(i,wordToken);
			result.addAll(tokenRelationships);	
		}
		return result;
	}
	
	private void setrelationshipMaps(List<PrepPhraseRelationshipMapping> relationshipMappings){
		relationshipMap  = new HashMap<>();
		semanticTypeRelationshipMap = new HashMap<>();
		for(PrepPhraseRelationshipMapping relationship : relationshipMappings){
			if(relationship.isTokenSemanticType())
				setRelationshipMap(semanticTypeRelationshipMap,relationship);
			else 
				setRelationshipMap(relationshipMap,relationship);
		}
	}

	private void setRelationshipMap(Map<String, List<PrepPhraseRelationshipMapping>> map,PrepPhraseRelationshipMapping relationship){
		if(!map.containsKey(relationship.getToken()))
			map.put(relationship.getToken().toLowerCase(), new ArrayList<PrepPhraseRelationshipMapping>());
		map.get(relationship.getToken().toLowerCase()).add(relationship);
	}
	
	private List<TokenRelationship> getTokenRelationship(int index, WordToken token){
		if(index==0)return null;
		List<TokenRelationship> result = new ArrayList<TokenRelationship>();
		WordToken previousToken = wordTokens.get(index-1);
				
		for(int i = index+1;i<=wordTokens.size()-1;i++){
			WordToken prepToken = wordTokens.get(i);
			if(prepToken.getPropertyValueType()==null) continue;
			if(!prepToken.getPropertyValueType().equals(PropertyValueTypes.PrepPhraseEnd)) continue;
			
			PrepPhraseRelationshipMapping relationshipMapping = findMapping(this.relationshipMap, token,previousToken, prepToken);
			if(relationshipMapping!=null){
				result.add(createTokenRelationship(relationshipMapping.getEdgeName(),this.relatedFrameName, previousToken, prepToken));
				continue;
			}
			 relationshipMapping = findMapping(this.semanticTypeRelationshipMap, token,previousToken, prepToken);
			if(relationshipMapping!=null) 
			{
				result.add(createTokenRelationship(relationshipMapping.getEdgeName(),this.relatedFrameName, previousToken, prepToken));	
				continue;
			}
			result.add(createTokenRelationship(null,this.modifierFrameName,previousToken,prepToken));
		}
		return result;
	}
	
	private PrepPhraseRelationshipMapping findMapping(Map<String, List<PrepPhraseRelationshipMapping>> map, WordToken currnentToken, WordToken previousToken, WordToken prepPhraseToken){
		String key = currnentToken.getToken().toLowerCase();
		if(!map.containsKey(key)) return null;
		
		for(PrepPhraseRelationshipMapping relationshipMapping : map.get(key)){
			if(!isWordTokenMatchToRelationship(relationshipMapping.isPreviousTokenSemanticType(),relationshipMapping.isPreviousTokenPOSType(),relationshipMapping.getPreviousToken(), previousToken))
				continue;
			if(isWordTokenMatchToRelationship(relationshipMapping.isPrepObjectTokenSemanticType(), relationshipMapping.isPrepObjectTokenPOSType(), relationshipMapping.getPrepObjectToken(),prepPhraseToken))
				return relationshipMapping;
		}
		return null;
	}
		
	private TokenRelationship createTokenRelationship(String edgeName,String frameName, WordToken fromToken, WordToken toToken){
		return this.tokenRelationshipFactory.create(edgeName, frameName, fromToken, toToken);
	}
}


 