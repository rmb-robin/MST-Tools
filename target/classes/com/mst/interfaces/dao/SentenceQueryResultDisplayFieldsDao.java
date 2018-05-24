package com.mst.interfaces.dao;

import com.mst.model.SentenceQuery.SentenceQueryResultDisplayFields;

public interface SentenceQueryResultDisplayFieldsDao extends IDao {

	String save(SentenceQueryResultDisplayFields fields);
	SentenceQueryResultDisplayFields getByOrgId(String orgId);
}
