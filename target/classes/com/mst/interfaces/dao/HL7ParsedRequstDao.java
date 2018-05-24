package com.mst.interfaces.dao;

import java.util.List;

import com.mst.model.raw.HL7ParsedRequst;
import com.mst.model.requests.SentenceTextRequest;

public interface HL7ParsedRequstDao  { 
	String save(HL7ParsedRequst request);
	HL7ParsedRequst filter(SentenceTextRequest request);
	HL7ParsedRequst  get(String id);
	List<HL7ParsedRequst> getAll();
}
