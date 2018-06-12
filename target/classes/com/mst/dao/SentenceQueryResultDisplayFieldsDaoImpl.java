package com.mst.dao;
import org.mongodb.morphia.query.Query;

import com.mst.interfaces.dao.SentenceQueryResultDisplayFieldsDao;
import com.mst.model.SentenceQuery.SentenceQueryResultDisplayFields;
import com.mst.model.raw.HL7ParsedRequst;

public class SentenceQueryResultDisplayFieldsDaoImpl extends BaseDocumentDaoImpl<SentenceQueryResultDisplayFields> implements SentenceQueryResultDisplayFieldsDao {

	public SentenceQueryResultDisplayFieldsDaoImpl() {
		super(SentenceQueryResultDisplayFields.class);
	}

	@Override
	public SentenceQueryResultDisplayFields getByOrgId(String orgId) {
		Query<SentenceQueryResultDisplayFields> q = this.getDatastore().createQuery(SentenceQueryResultDisplayFields.class);
		q
		 .field("organizationId").equal(orgId);
		return q.get();	
	}
}
