package com.mst.interfaces;

import java.time.LocalDate;
import java.util.List;

import com.mst.interfaces.dao.IDao;
import com.mst.model.discrete.DiscreteData;

public interface DiscreteDataDao  extends IDao {
	List<DiscreteData> getByNameAndDate(String orgName, LocalDate date);	
	long getCountByNameAndDate(String orgName, LocalDate date);
	
}
