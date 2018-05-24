package com.mst.dao;

import org.mongodb.morphia.query.Query;

import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.dao.RawReportFileDao;
import com.mst.model.raw.HL7ParsedRequst;
import com.mst.model.raw.RawReportFile;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.util.MongoConnectionProvider.DbProviderType;

public class RawReportFileDaoImpl extends BaseDocumentDaoImpl<RawReportFile> implements RawReportFileDao {


	public RawReportFileDaoImpl() {
		super(RawReportFile.class, DbProviderType.rawDb);
	}

	public String save(RawReportFile reportFile) {
		return super.save(reportFile);
	}


}
