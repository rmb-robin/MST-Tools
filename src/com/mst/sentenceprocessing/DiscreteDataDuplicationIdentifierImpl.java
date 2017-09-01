package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mst.interfaces.sentenceprocessing.DiscreteDataDuplicationIdentifier;
import com.mst.model.discrete.DiscreteData;

public class DiscreteDataDuplicationIdentifierImpl implements DiscreteDataDuplicationIdentifier {

	private Map<String, List<DiscreteData>> discreteDatabyVRID; 
	
	public DiscreteDataDuplicationIdentifierImpl(){
		discreteDatabyVRID = new HashMap<>();
	}
	
	
	public void process(List<DiscreteData> discreteDatas){
		for(DiscreteData discreteData: discreteDatas){
			if(discreteData.getOrderControl()!=null && discreteData.getOrderControl().equals("CN")) 
				discreteData.setIsDuplicate(true);
			String vrId = discreteData.getVrReportId();
			if(!discreteDatabyVRID.containsKey(vrId))
				discreteDatabyVRID.put(vrId, new ArrayList<>());
			discreteDatabyVRID.get(vrId).add(discreteData);	
		}
		updateDuplicateVRIds();
	}
	
	private void updateDuplicateVRIds(){
		for(Map.Entry<String, List<DiscreteData>> entry:discreteDatabyVRID.entrySet()){
			if(entry.getValue().size()>1)
				entry.getValue().forEach(a-> a.setIsDuplicate(true));
		}
	}
}
