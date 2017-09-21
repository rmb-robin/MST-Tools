package com.mst.sentenceprocessing;

import java.util.List;
import java.util.Map.Entry;

import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.sentenceProcessing.RecommandedTokenRelationship;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

public class RecommendationEdgesVerificationProcesser {

	public List<RecommandedTokenRelationship> process(SentenceDiscovery sentenceDiscovery, List<RecommandedTokenRelationship> existing){
		for(Entry<Integer, Integer> entry: sentenceDiscovery.getNounPhraseIndexes().entrySet()){
			setVerifiedStatus(entry.getKey(), entry.getValue(), sentenceDiscovery.getWordEmbeddings());
		}
		return sentenceDiscovery.getWordEmbeddings();
	}
	
	
	private void setVerifiedStatus(int beginIndex,int endIndex, List<RecommandedTokenRelationship> embeddedwords){
//		if(beginIndex>=endIndex) return; 
//		for(int i = beginIndex;i<=endIndex;i++)
//		{
//			RecommandedTokenRelationship start = embeddedwords.get(i);
//			if(!start.getTokenRelationship().getEdgeName().equals(WordEmbeddingTypes.defaultEdge)){
//				start.setIsVerified(true);
//				continue;
//			}
//			
//			
//			WordToken from = start.getTokenRelationship().getFromToken();
//			WordToken end = embeddedwords.get(endIndex).getTokenRelationship().getToToken();
//			
//			//only check if two t-t are sequential...
//			if(isMatchOnFromTo(from,end, embeddedwords)){
//				{
//
//						
//					
//					
////Does this pick up after we find...
////If there are two or more sequential edgeName token-token edges, after isVerified status is determined:
////
////a) If the fromToken of the first token-token modifies the toToken of the second token-token,
//				          //then set first token-token isVerified == false. 
//				          //Create new token-token edgeName (in the same object in sentenceDiscoveries) 
//				          //between fromToken and toToken (including token indexs of each token with isVerified == true.
////
////
////
////
////b) If the fromToken of the first token-token modifies the toToken of the first token-token, then set the first token-token isVerified == true.
//
//					
//					
//					
//					
//			
//			
//			
//			//if we have two sequential token/token. 
//			//and no word embedding
//			
//			
//			
//			
//			embeddedwords.get(i).setIsVerified(true);
//		}

	}
	
	private boolean isMatchOnFromTo(WordToken from, WordToken to, List<RecommandedTokenRelationship> embeddedwords){
		for(RecommandedTokenRelationship recommandedTokenRelationship: embeddedwords){
			TokenRelationship tokenRelationship = recommandedTokenRelationship.getTokenRelationship();
			if(!from.getToken().equals(tokenRelationship.getFromToken().getToken())) continue;
			if(!to.getToken().equals(tokenRelationship.getToToken().getToken())) continue;
			return true;
		}
		return false;
	}
	
}
