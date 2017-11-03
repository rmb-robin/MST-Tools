package com.mst.interfaces.filter;

import java.util.List;

import com.mst.model.recommandation.SentenceDiscovery;

public interface SentenceDiscoveryFilter {
	List<SentenceDiscovery> filter(List<SentenceDiscovery> sentenceDiscoveries, String text);
}
