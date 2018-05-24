package com.mst.interfaces.filter;

import com.mst.model.SentenceQuery.SentenceQueryInput;

public interface NotAndAllRequestFactory {

	SentenceQueryInput create(SentenceQueryInput input);
}
