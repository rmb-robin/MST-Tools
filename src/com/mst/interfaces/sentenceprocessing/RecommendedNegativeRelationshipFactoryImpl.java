package com.mst.interfaces.sentenceprocessing;

import java.util.ArrayList;
import java.util.List;

import com.mst.model.metadataTypes.EdgeTypes;
import com.mst.model.metadataTypes.PartOfSpeachTypes;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;
import com.mst.sentenceprocessing.TokenRelationshipFactoryImpl;

public class RecommendedNegativeRelationshipFactoryImpl {

	private TokenRelationshipFactory relationshipFactory = new TokenRelationshipFactoryImpl();
	
	public List<RecommendedTokenRelationship> create(List<WordToken> wordTokens){
		List<RecommendedTokenRelationship> result = new ArrayList<>();
		
		for(int i =0;i<wordTokens.size();i++){
			WordToken wordToken = wordTokens.get(i);
			if(wordToken.getPos()==null) continue;
			if(!wordToken.getPos().equals(PartOfSpeachTypes.NEG))continue;
			RecommendedTokenRelationship edge = createEdge(i, wordTokens);
			if(edge!=null) result.add(edge);
		}		
		return result;
	}

	private RecommendedTokenRelationship createEdge(int index, List<WordToken> wordTokens){
		
		String sourceName = this.getClass().getName();
		
		if(index>0){
			WordToken prevWordToken = wordTokens.get(index-1);
			if(prevWordToken.isVerb())
			{
				return relationshipFactory.createRecommendedRelationship(WordEmbeddingTypes.verbPlusNeg, EdgeTypes.related, prevWordToken, wordTokens.get(index),sourceName);
			}
		}
		
		if(index<wordTokens.size()){
			WordToken nextWordToken = wordTokens.get(index+1);
			if(nextWordToken.isVerb()){
				return relationshipFactory.createRecommendedRelationship(WordEmbeddingTypes.verbMinusNeg, EdgeTypes.related, wordTokens.get(index),nextWordToken,sourceName);
			}
			
			return relationshipFactory.createRecommendedRelationship(WordEmbeddingTypes.negativeToken, EdgeTypes.related, wordTokens.get(index),nextWordToken,sourceName);
		}
		return null;
			
	}
	
	
}
