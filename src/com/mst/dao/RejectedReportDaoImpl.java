package com.mst.dao;

import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.dao.RejectedReportDao;
import com.mst.model.requests.RejectedReport;

public class RejectedReportDaoImpl extends BaseDocumentDaoImpl<RejectedReport> implements RejectedReportDao {

	public RejectedReportDaoImpl() {
		super(RejectedReport.class);
	}

	public void setMongoDatastoreProvider(MongoDatastoreProvider provider) {
		super.setMongoDatastoreProvider(provider);
	}
}
