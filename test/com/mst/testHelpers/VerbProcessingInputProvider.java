package com.mst.testHelpers;

import java.util.List;
import java.util.UUID;

import com.mst.model.gentwo.ActionVerbItem;
import com.mst.model.gentwo.ActionVerbTable;
import com.mst.model.gentwo.VerbProcessingInput;
import com.mst.model.gentwo.VerbTense;

public class VerbProcessingInputProvider {

	private VerbProcessingInput verbProcessingInput;
	private ActionVerbTable acttionVerbTable;
	private String getFullFilePath(String file){
		return System.getProperty("user.dir") + "\\testData\\" + file;
	}
	
	public VerbProcessingInput getInput(){
		verbProcessingInput = new VerbProcessingInput();
		acttionVerbTable = new ActionVerbTable();
		verbProcessingInput.setActionVerbTable(acttionVerbTable);
		String path = getFullFilePath("actionVerbTable.txt");
		List<String> lines = TestDataProvider.readLines(path);
		lines.forEach((t) -> processLine(t));
		return verbProcessingInput;
	}
	
	private void processLine(String line){
		String[] contents = line.split(",");
		ActionVerbItem infinitivePresentItem = createActionVerbItem(VerbTense.InfinitivePresent, null,contents[0]);
		UUID infinitivePresentItemId = infinitivePresentItem.getId();
		acttionVerbTable.addValue(infinitivePresentItem);
	
		acttionVerbTable.addValue(createActionVerbItem(VerbTense.PluralInfinitivePresent,infinitivePresentItemId, contents[1]));
		acttionVerbTable.addValue(createActionVerbItem(VerbTense.Past,infinitivePresentItemId, contents[2]));
		acttionVerbTable.addValue(createActionVerbItem(VerbTense.Present,infinitivePresentItemId, contents[3]));
	}
	
	private ActionVerbItem createActionVerbItem(VerbTense tense, UUID infinitivePresentId, String verb){
		ActionVerbItem item = new ActionVerbItem();
		if(infinitivePresentId ==null){
			UUID id = UUID.randomUUID();
			infinitivePresentId =  id;
			item.setId(id);
		}
		else 
			item.setId( UUID.randomUUID());
		
		item.setInfinitivePresentId(infinitivePresentId);
		
		item.setVerb(verb);
		item.setVerbTense(tense);
		return item;
	}
}
