package com.mst.interfaces.dao;

import com.mst.model.requests.RejectedReport;

public interface RejectedReportDao extends IDao {

	String save(RejectedReport rejectedReport);
}
