package com.mst.model.gentwo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class VerbProcessingInput {
	private ActionVerbTable actionVerbTable;
	private Map<String, LinkingModalVerbItem> linkingModalVerbMap;
	private HashSet<String> infinitiveSignals; 
	private String infinitiveSignalPosType; 
	
	
	public VerbProcessingInput(){
		linkingModalVerbMap = new HashMap<String, LinkingModalVerbItem>();
		infinitiveSignals =new HashSet<>();
	}
	
	public ActionVerbTable getActionVerbTable() {
		return actionVerbTable;
	}

	public void setActionVerbTable(ActionVerbTable actionVerbTable) {
		this.actionVerbTable = actionVerbTable;
	}


	public Map<String, LinkingModalVerbItem> getLinkingModalVerbMap() {
		return linkingModalVerbMap;
	}

	public void setLinkingModalVerbMap(Map<String, LinkingModalVerbItem> linkingModalVerbMap) {
		this.linkingModalVerbMap = linkingModalVerbMap;
	}

	public HashSet<String> getInfinitiveSignals() {
		return infinitiveSignals;
	}

	public void setInfinitiveSignals(HashSet<String> infinitiveSignals) {
		this.infinitiveSignals = infinitiveSignals;
	}

	public String getInfinitiveSignalPosType() {
		return infinitiveSignalPosType;
	}

	public void setInfinitiveSignalPosType(String infinitiveSignalPosType) {
		this.infinitiveSignalPosType = infinitiveSignalPosType;
	}
	
}
