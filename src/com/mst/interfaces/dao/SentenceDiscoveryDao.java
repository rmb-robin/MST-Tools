package com.mst.interfaces.dao;

import java.util.List;

import com.mst.model.recommandation.SentenceDiscovery;

public interface SentenceDiscoveryDao extends IDao {

	String save(SentenceDiscovery sentenceDiscovery);
	void saveSentenceDiscovieries(List<SentenceDiscovery> sentenceDiscoveries);
}
