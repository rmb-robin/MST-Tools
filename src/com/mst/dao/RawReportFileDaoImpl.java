package com.mst.dao;

import com.mst.interfaces.MongoDatastoreProvider;
import com.mst.interfaces.dao.RawReportFileDao;
import com.mst.model.raw.RawReportFile;

public class RawReportFileDaoImpl extends BaseDocumentDaoImpl<RawReportFile> implements RawReportFileDao {


	public RawReportFileDaoImpl() {
		super(RawReportFile.class);
		
	}

	public String save(RawReportFile reportFile) {
		return super.save(reportFile);
	}
}
