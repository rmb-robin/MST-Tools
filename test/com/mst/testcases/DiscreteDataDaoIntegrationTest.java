package com.mst.testcases;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import org.junit.Test;
import com.mst.dao.DiscreteDataDaoImpl;
import com.mst.util.MongoDatastoreProviderDefault;
 

public class DiscreteDataDaoIntegrationTest {

	@Test
	public void getQueryByOrgNameAndDate(){
		DiscreteDataDaoImpl dao = new DiscreteDataDaoImpl();
		dao.setMongoDatastoreProvider(new MongoDatastoreProviderDefault());
		long count = dao.getCountByNameAndDate("orgName-Test",  LocalDate.of(2017,6, 1));
		assertEquals(2, count);
	}
}
