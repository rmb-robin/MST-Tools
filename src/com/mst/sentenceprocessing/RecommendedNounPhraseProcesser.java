package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mst.model.metadataTypes.PropertyValueTypes;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.sentenceProcessing.RecommandedNounPhraseResult;
import com.mst.model.sentenceProcessing.RecommandedTokenRelationship;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

public class RecommendedNounPhraseProcesser {

	public RecommandedNounPhraseResult process(List<RecommandedTokenRelationship> embeddedwords){
		
		Map<Integer, RecommandedTokenRelationship> wordEmbeddingsByIndex = getFilteredWordEmbeddings(embeddedwords);
		int iterator = 0;
		int prevIndex=0;
		int beginNounPhraseIndex=0; 
		int endNounPhraseIndex=0; 
		Map<Integer, Integer> nounPhraseIndexes = new HashMap<Integer,Integer>();
		for(Entry<Integer,RecommandedTokenRelationship> entry : wordEmbeddingsByIndex.entrySet()){
			TokenRelationship tokenRelationship = entry.getValue().getTokenRelationship();
			 
			if(iterator==0 || entry.getKey()-prevIndex>1){
				tokenRelationship.getFromToken().setPropertyValueType(PropertyValueTypes.NounPhraseBegin);
				beginNounPhraseIndex = entry.getKey();
			}
			if(!wordEmbeddingsByIndex.containsKey(entry.getKey()+1)) {
				tokenRelationship.getToToken().setPropertyValueType(PropertyValueTypes.NounPhraseEnd);
				endNounPhraseIndex = entry.getKey();
				if(!nounPhraseIndexes.containsKey(beginNounPhraseIndex))
					nounPhraseIndexes.put(beginNounPhraseIndex, endNounPhraseIndex);
				//setVerifiedStatus(beginNounPhraseIndex, endNounPhraseIndex, embeddedwords);
			}	
			if(iterator == wordEmbeddingsByIndex.size()-1) break;
			prevIndex = entry.getKey();
			iterator+=1;
		}	
		RecommandedNounPhraseResult nounPhraseResult = new RecommandedNounPhraseResult();
		nounPhraseResult.setRecommandedTokenRelationships(embeddedwords);
		nounPhraseResult.setNounPhraseIndexes(nounPhraseIndexes);
		return nounPhraseResult;
	}

	private Map<Integer,RecommandedTokenRelationship> getFilteredWordEmbeddings(List<RecommandedTokenRelationship> wordEmbeddings){
		Map<Integer,RecommandedTokenRelationship> result = new HashMap<>();
		for(int i = 0;i<wordEmbeddings.size();i++){
			RecommandedTokenRelationship recommandedTokenRelationship =  wordEmbeddings.get(i);
			TokenRelationship tokenRelationship = recommandedTokenRelationship.getTokenRelationship();
			String type = tokenRelationship.getEdgeName();
			if(type.equals(WordEmbeddingTypes.defaultEdge))
					result.put(i, recommandedTokenRelationship);
		}
		return result;
	}
}
