package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.List;

import com.mst.interfaces.RelationshipProcessor;
import com.mst.model.WordToken;
import com.mst.model.gentwo.PartOfSpeachTypes;
import com.mst.model.gentwo.PropertyValueTypes;
import com.mst.model.gentwo.RelationshipInput;
import com.mst.model.gentwo.RelationshipMapping;
import com.mst.model.gentwo.TokenRelationship;



public class PrepPhraseRelationshipProcessor extends RelationshipProcessorBase implements RelationshipProcessor {

	private String modifierFrameName = "f_modifier";
	private String relatedFrameName = "f_related";
	
	public List<TokenRelationship> process(List<WordToken> tokens, RelationshipInput input) {
		this.wordTokens = tokens;
		return createRelationships();
	}
	
	private List<TokenRelationship> createRelationships(){
		List<TokenRelationship> result = new ArrayList<>();
		
		for(int i =0;i<wordTokens.size()-1;i++){
			WordToken wordToken = wordTokens.get(i);
			if(wordToken.getPos()!=PartOfSpeachTypes.IN) continue;
			TokenRelationship tokenRelationship = getRelatedModifiedRelation(i);
			if(tokenRelationship!=null) 
				result.add(tokenRelationship);	
		}
		return result;
	}
	
	private TokenRelationship getRelatedModifiedRelation(int index){
		if(index==0)return null;
		WordToken previousToken = wordTokens.get(index-1);
				
		for(int i = index+1;i<=wordTokens.size()-1;i++){
			WordToken toToken = wordTokens.get(i);
			if(toToken.getPos().equals(PropertyValueTypes.PrepPhraseEnd))
			{
				RelationshipMapping relationshipMapping = findMapping(this.relationshipMap, previousToken, toToken);
				if(relationshipMapping!=null) 
					return createTokenRelationship(relationshipMapping.getEdgeName(),this.relatedFrameName, previousToken, toToken);
				
				 relationshipMapping = findMapping(this.semanticTypeRelationshipMap, previousToken, toToken);
					if(relationshipMapping!=null) 
						return createTokenRelationship(relationshipMapping.getEdgeName(),this.relatedFrameName, previousToken, toToken);				
				return createTokenRelationship(null,this.modifierFrameName,previousToken,toToken);
			}
		}
		return null;
	}
	
	private TokenRelationship createTokenRelationship(String edgeName,String frameName, WordToken fromToken, WordToken toToken){
		return this.tokenRelationshipFactory.create(edgeName, frameName, fromToken, toToken);
	}
}


 