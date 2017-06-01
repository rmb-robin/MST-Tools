package com.mst.interfaces.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.mst.model.requests.RejectedReport;

public interface RejectedReportDao extends IDao {

	String save(RejectedReport rejectedReport);
	List<RejectedReport> getByNameAndDate(String orgName, LocalDate localDate);
}
