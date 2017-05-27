package com.mst.interfaces.dao;

import java.util.List;

import com.mst.model.HL7Details;

public interface Hl7DetailsDao extends IDao {

	List<HL7Details> getByOrgName(String orgName);
	
}
