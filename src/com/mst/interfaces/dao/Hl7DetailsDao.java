package com.mst.interfaces.dao;

import java.util.List;

import com.mst.model.HL7Details;
import com.mst.model.raw.AllHl7Elements;

public interface Hl7DetailsDao extends IDao {

	List<HL7Details> getByOrgName(String orgName);
	AllHl7Elements getAllElements();
	void saveAllElements(AllHl7Elements allHl7Elements);
}
