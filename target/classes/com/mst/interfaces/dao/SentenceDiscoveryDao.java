package com.mst.interfaces.dao;

import java.util.List;

import com.mst.model.discrete.DiscreteData;
import com.mst.model.recommandation.SentenceDiscovery;
import com.mst.model.sentenceProcessing.SentenceProcessingFailures;

public interface SentenceDiscoveryDao extends IDao {

	String save(SentenceDiscovery sentenceDiscovery);
	void saveSentenceDiscovieries(List<SentenceDiscovery> sentenceDiscoveries);
	void saveSentenceDiscoveries(List<SentenceDiscovery> sentenceDiscoveries, DiscreteData discreteData,SentenceProcessingFailures failures);
}
