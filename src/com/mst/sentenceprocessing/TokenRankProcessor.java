package com.mst.sentenceprocessing;

import com.mst.model.recommandation.SentenceDiscovery;

public interface TokenRankProcessor {
	/**
	 * process and calculate token ranking of wordtoken based on wordembedding type
	 * @param sentenceDiscovery
	 */
	public void setTokenRankings(SentenceDiscovery sentenceDiscovery);

}
