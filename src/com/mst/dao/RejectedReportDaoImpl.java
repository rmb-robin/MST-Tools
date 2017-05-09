package com.mst.dao;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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

	public List<RejectedReport> getByNameAndDate(String orgName, LocalDate localDate) {
		
	
//		Instant instant = localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
//		Date date = Date.from(instant);
//		
		Query<RejectedReport> query = datastoreProvider.getDataStore().createQuery(RejectedReport.class);
		 query
		 	.field("organizationName").equal(orgName);
		 	//.field("processingDate").equal(localDate);
		 return query.asList();
	}
}
