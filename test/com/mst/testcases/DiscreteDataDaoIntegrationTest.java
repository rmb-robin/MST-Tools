package com.mst.testcases;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.Test;
import com.mst.dao.DiscreteDataDaoImpl;
import com.mst.model.SentenceQuery.DiscreteDataFilter;
import com.mst.model.discrete.DiscreteData;
import com.mst.util.MongoDatastoreProviderDefault;
 

public class DiscreteDataDaoIntegrationTest {

	@Test
	public void getQueryByOrgNameAndDate(){
		DiscreteDataDaoImpl dao = new DiscreteDataDaoImpl();
		dao.setMongoDatastoreProvider(new MongoDatastoreProviderDefault());
		long count = dao.getCountByNameAndDate("orgName-Test",  LocalDate.of(2017,6, 1));
		assertEquals(2, count);
	}
	
	@Test
	public void getDiscreteDataIdsTest(){
		DiscreteDataFilter dataFilter = new DiscreteDataFilter();
		List<String> gender = new ArrayList<>();
		gender.add("F");
		dataFilter.setPatientSex(gender);
		
		DiscreteDataDaoImpl dao = new DiscreteDataDaoImpl();
		dao.setMongoDatastoreProvider(new MongoDatastoreProviderDefault());
		List<DiscreteData> ids = dao.getDiscreteDataIds(dataFilter,"58c6f3ceaf3c420b90160803");
		
		int t = ids.size();
	
	}
}
