package com.mst.interfaces;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;

import com.mst.interfaces.dao.IDao;
import com.mst.model.SentenceQuery.DiscreteDataFilter;
import com.mst.model.discrete.DiscreteData;

public interface DiscreteDataDao  extends IDao {
	List<DiscreteData> getByNameAndDate(String orgName, LocalDate date);	
	long getCountByNameAndDate(String orgName, LocalDate date);
	List<DiscreteData> getDiscreteDatas(DiscreteDataFilter dataFilter, String orgId,boolean allvalues);
	List<DiscreteData> getByIds(Set<String> ids);
	DiscreteData getbyid(String id);
	String save(DiscreteData discreteData, boolean isReprocess);
	void saveCollection(List<DiscreteData> discreteDatas);
}
