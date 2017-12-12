package com.mst.dao;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.mongodb.morphia.query.Query;

import com.mst.interfaces.dao.HL7ParsedRequstDao;
import com.mst.model.raw.HL7ParsedRequst;
import com.mst.model.requests.SentenceTextRequest;
import com.mst.util.MongoConnectionProvider.DbProviderType;


public class HL7ParsedRequstDaoImpl   extends BaseDocumentDaoImpl<HL7ParsedRequst> implements HL7ParsedRequstDao {
 public HL7ParsedRequstDaoImpl() {
		super(HL7ParsedRequst.class, DbProviderType.rawDb);
	}
 
 
	@Override
	public HL7ParsedRequst filter(SentenceTextRequest request) {
	
		Query<HL7ParsedRequst> q = this.getDatastore().createQuery(HL7ParsedRequst.class);
		q
		 .field("discreteData.accessionNumber").equal(request.getDiscreteData().getAccessionNumber())
		 .field("discreteData.resultStatus").equal(request.getDiscreteData().getResultStatus())
		 .field("discreteData.readingLocation").equals(request.getDiscreteData().getReadingLocation());
		
		if(request.getDiscreteData().getReportFinalizedDate()!=null){
			LocalDate date = request.getDiscreteData().getReportFinalizedDate();
			addDateQuery(date, q, true);
			addDateQuery(date, q, false);
		}
	    return q.get();	
	}
	
	private void addDateQuery(LocalDate localdate, Query<HL7ParsedRequst> query, boolean isFirst){
		Date date = Date.from(localdate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		
		if(isFirst)
			query.filter("discreteData.reportFinalizedDate >= ", date);
		else 
		{
			LocalDate nextLocaldate = localdate.plusDays(1);
			Date nexDate = Date.from(nextLocaldate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
			query.filter("discreteData.reportFinalizedDate < ", nexDate);	
		}
		
	}
}
