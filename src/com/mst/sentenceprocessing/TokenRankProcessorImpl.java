package com.mst.sentenceprocessing;

import java.util.Collections;

import java.util.List;

//import com.mst.model.GenericToken;
import com.mst.model.metadataTypes.WordEmbeddingTypes;
import com.mst.model.recommandation.RecommendedTokenRelationship;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

/**
 * 
 * @author Rabhu
 * Class created to calculate the tokenRanking on 07/03/2018
 *
 */

public class TokenRankProcessorImpl  implements TokenRankProcessor{
	private static final String COMMA = ",";
	private static final String HYPHEN = "-";
	private static final String SEMICOLON = ";";
	
	/*
	 * Tasks:

a) Add tokenValue to modifiedWordList tokens. 
b) Add logic to verification processor:

1) If wordEmbedding == prepMinus, increment fromToken tokenValue by 4
2) If wordEmbedding == tokentoken, increment toToken tokenValue by 1
3) If wordEmbedding == tokentoken and tokentoken == highest index tokentoken, increment toToken tokenValue by 2
4) If wordEmbedding == token-token and toToken==, increment fromToken tokenValue by 2 
5) If wordEmbedding == token-token and toToken== ; increment fromToken tokenValue by 2
6) If wordEmbedding == token-token and toToken == -, increment fromToken tokenValue by 2
7) If wordEmbedding == prepMinus and toToken == fromToken in token-token and toToken in token-token == fromToken == prep-1, then increment tokenValue by 1
	 */
	
	/**
	 * Method setTokenRankings is used to check various conditions and assign token ranking when the conditions meet. 
	 * Method get's the edgeName from the embeddedWords and compares it with WordEmbeddingTypes. When the required condition is matched
	 * the statements inside the if condition assigns the tokenRanking as required.
	 * @param embeddedWords
	 * 
	 */
		
	public void setTokenRankings(SentenceDiscovery sentenceDiscovery){
		//WordToken wordtoken = new WordToken();
		List<RecommendedTokenRelationship> embeddedWords = sentenceDiscovery.getWordEmbeddings();
		Collections.reverse(embeddedWords);
		/*
		String prevToToken = null;
		int incrementValue=4;
		Boolean flag = false;
		String tokenTokenToToken=null;
		*/
		for(int i =0; i<embeddedWords.size();i++) {
			RecommendedTokenRelationship recommendedTokenRelationship = embeddedWords.get(i);
			TokenRelationship relationship = recommendedTokenRelationship.getTokenRelationship();
			String edgeName = relationship.getEdgeName(); 
			WordToken toToken = relationship.getToToken();
			WordToken fromToken = relationship.getFromToken();
			//String prevToToken = null;
				
			int tokenRanking=0;
			
			
			if(edgeName.equals(WordEmbeddingTypes.prepMinus)) {
				/*
				if(flag) {
					tokenRanking =fromToken.getTokenRanking()+incrementValue;
				}
				else {
					tokenRanking =fromToken.getTokenRanking()+4;	
				}
				*/
				tokenRanking =fromToken.getTokenRanking()+4;
				fromToken.setTokenRanking(tokenRanking);
				updateModifiedWordList(sentenceDiscovery, fromToken.getToken(), tokenRanking);
				//prevToToken = toToken.getToken();
				//incrementValue--;
				continue;
			}
			/*
			if(edgeName.equals(WordEmbeddingTypes.prepPlus)) {
				prevToToken = toToken.getToken();
			}
			*/
			
			if(edgeName.equals(WordEmbeddingTypes.tokenToken)) {
				if(SEMICOLON.equals(toToken.getToken())||HYPHEN.equals(toToken.getToken())||COMMA.equals(toToken.getToken())) {
					tokenRanking = fromToken.getTokenRanking()+2;
					fromToken.setTokenRanking(tokenRanking);
					continue;
				}
				if (i+1 < embeddedWords.size()) {
					RecommendedTokenRelationship nextRecommendedTokenRelationship = embeddedWords.get(i+1);
					TokenRelationship nextRelationship = nextRecommendedTokenRelationship.getTokenRelationship();
					String nextEdgeName = nextRelationship.getEdgeName();
					if(nextEdgeName.equals(WordEmbeddingTypes.tokenToken)) {
						WordToken nextToToken = nextRelationship.getToToken();	
						WordToken nextFromToken = nextRelationship.getFromToken();	
						/*
						 * In the following if statement, we check whether the nextToToken is a ;,-.
						 * If yes, then we don't do anything and continue for next iteration.
						 */
						
						if(SEMICOLON.equals(nextToToken.getToken())||HYPHEN.equals(nextToToken.getToken())||COMMA.equals(nextToToken.getToken())){
							
							continue;
						}
						
						tokenRanking =nextToToken.getTokenRanking()+2;
						nextToToken.setTokenRanking(tokenRanking);
						
						/*
						 * This getToken() is returning "lung" so word=lung which is incorrect. 
						 * Tracing back to getToken(), I figured out that the toToken for nextToToken has never been set 
						 */
						//String word = nextToToken.getToken();	
						//updateModifiedWordList(sentenceDiscovery, word, tokenRanking);
						//updateModifiedWordListNew(sentenceDiscovery, nextRelationship.getToToken(), tokenRanking);
						continue;
					}
				}
				/*
				if(prevToToken.equals(fromToken.getToken())) {
					tokenRanking = toToken.getTokenRanking()+incrementValue;
					flag = true;
					incrementValue--;
					tokenTokenToToken = toToken.getToken();
				}
				*/
				tokenRanking = toToken.getTokenRanking()+1;
				toToken.setTokenRanking(tokenRanking);
				updateModifiedWordList(sentenceDiscovery, toToken.getToken(), tokenRanking);
				
			}
				
		}
	}	
	private void updateModifiedWordList(SentenceDiscovery sentenceDiscovery, String word, int tokenRanking) {
			
			for (WordToken wordToken : sentenceDiscovery.getModifiedWordList()) {
				if(wordToken.getToken().equals(word)) {
					wordToken.setTokenRanking(tokenRanking);
				}
			}
	}
	
}
