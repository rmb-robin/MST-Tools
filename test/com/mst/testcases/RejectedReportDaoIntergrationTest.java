package com.mst.testcases;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import com.mst.dao.RejectedReportDaoImpl;
import com.mst.dao.SentenceDaoImpl;
import com.mst.model.requests.RejectedReport;
import com.mst.model.sentenceProcessing.SentenceDb;
import com.mst.util.MongoDatastoreProviderDefault;
import static org.junit.Assert.*;

public class RejectedReportDaoIntergrationTest {

	//@Test
	public void getRejectedReport(){
		
		LocalDate date = LocalDate.of(2017,6, 1);
		RejectedReportDaoImpl dao = new RejectedReportDaoImpl();
		dao.setMongoDatastoreProvider(new MongoDatastoreProviderDefault());
		List<RejectedReport> rejectedReports =  dao.getByNameAndDate("orgName-Test", date);
		assertEquals(1,rejectedReports.size());
	}
	
	
}
