package com.mst.interfaces.dao;

import com.mst.model.raw.HL7ParsedRequst;
import com.mst.model.requests.SentenceTextRequest;

public interface HL7ParsedRequstDao { 
	String save(HL7ParsedRequst request);
	HL7ParsedRequst filter(SentenceTextRequest request);
}
