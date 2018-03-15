package com.mst.testcases;

import java.util.List;

import org.junit.Test;

import com.mst.dao.DisceteDataComplianceDisplayFieldsDaoImpl;
import com.mst.interfaces.dao.DisceteDataComplianceDisplayFieldsDao;
import com.mst.metadataProviders.DiscreteDataComplianceFieldProvider;
import com.mst.model.discrete.DisceteDataComplianceDisplayFields;
import com.mst.util.MongoDatastoreProviderDefault;

public class LoadCompliance {

	@Test
	public void loadDiscreteDataComplianceFields(){
		DiscreteDataComplianceFieldProvider provider = new DiscreteDataComplianceFieldProvider();
		DisceteDataComplianceDisplayFields fields =  provider.get("rad","rad");
		DisceteDataComplianceDisplayFieldsDaoImpl dao = new DisceteDataComplianceDisplayFieldsDaoImpl();
		dao.setMongoDatastoreProvider(new MongoDatastoreProviderDefault());
		dao.save(fields);
		
		
//		List<DisceteDataComplianceDisplayFields> existing =  dao.getAll();
//	
//		for(DisceteDataComplianceDisplayFields complianceDisplayFields: existing){
//			fields.setOrgId(complianceDisplayFields.getOrgId());
//			dao.save(fields);
//			dao.delete(complianceDisplayFields.getId().toString());
//		}
	}
}
