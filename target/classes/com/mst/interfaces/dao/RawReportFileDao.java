package com.mst.interfaces.dao;

import com.mst.model.raw.RawReportFile;
import com.mst.model.requests.SentenceTextRequest;

public interface RawReportFileDao {

	String save(RawReportFile reportFile);
}
