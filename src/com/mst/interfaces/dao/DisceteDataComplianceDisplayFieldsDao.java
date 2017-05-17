package com.mst.interfaces.dao;

import com.mst.model.discrete.DisceteDataComplianceDisplayFields;

public interface DisceteDataComplianceDisplayFieldsDao extends IDao{
	String save(DisceteDataComplianceDisplayFields complianceDisplayFields);
	DisceteDataComplianceDisplayFields getbyOrgname(String orgName);
}
