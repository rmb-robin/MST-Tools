package com.mst.sentenceprocessing;

import com.mst.model.recommandation.SentenceDiscovery;
/**
 * 
 * @author Rabhu
 * Interface created to process TokenRank on 03/07/2018
 *
 */

public interface TokenRankProcessor {
	/**
	 * process and calculate token ranking of wordtoken based on wordembedding type
	 * @param sentenceDiscovery
	 */
	public void setTokenRankings(SentenceDiscovery sentenceDiscovery);

}
