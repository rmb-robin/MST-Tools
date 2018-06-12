package com.mst.dao;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.mongodb.morphia.query.Query;

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

	public List<RejectedReport> getByNameAndDate(String orgId, LocalDate localDate) {
		Date date = Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		Date nexDate = Date.from(localDate.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		Date prevDate = Date.from(localDate.plusDays(-1).atTime(11, 59).atZone(ZoneId.systemDefault()).toInstant());
		
		Query<RejectedReport> query = datastoreProvider.getDefaultDb().createQuery(RejectedReport.class);
		 query
		 	.field("organizationId").equal(orgId);
		 	
		 query
		 	.filter("processingDate >=", date)
		 	.filter("processingDate <", nexDate);
		 return query.asList();
	}
}
