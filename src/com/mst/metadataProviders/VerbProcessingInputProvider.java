package com.mst.metadataProviders;

import java.util.List;
import java.util.UUID;

import com.mst.interfaces.LinkingModalVerbItemFactory;
import com.mst.model.metadataTypes.VerbTense;
import com.mst.model.sentenceProcessing.ActionVerbItem;
import com.mst.model.sentenceProcessing.ActionVerbTable;
import com.mst.model.sentenceProcessing.LinkingModalVerbItem;
import com.mst.model.sentenceProcessing.VerbProcessingInput;
import com.mst.sentenceprocessing.LinkingModalVerbItemFactoryImpl;

public class VerbProcessingInputProvider {

	private VerbProcessingInput verbProcessingInput;
	private ActionVerbTable acttionVerbTable;
	private LinkingModalVerbItemFactory linkingModalVerbItemFactory; 
	private String getFullFilePath(String file){
		return System.getProperty("user.dir") + "\\testData\\" + file;
	}
	
	public VerbProcessingInput getInput(){
		linkingModalVerbItemFactory = new LinkingModalVerbItemFactoryImpl();
		
		verbProcessingInput = new VerbProcessingInput();
		populateActionVerbs();
		populateLinkingModalVerbs();
		verbProcessingInput.setInfinitiveSignalPosType("infinitiveSignal");
		verbProcessingInput.getInfinitiveSignals().add("to");
		return verbProcessingInput;
	}
	
	
	private void populateLinkingModalVerbs(){
		String path = getFullFilePath("linkingmodalverbinput.txt");
		List<String> lines = TestDataProvider.readLines(path);
		lines.forEach((t) -> processLinkingModalVerbLine(t));
	}
	
	private void populateActionVerbs(){
		acttionVerbTable = new ActionVerbTable();
		verbProcessingInput.setActionVerbTable(acttionVerbTable);
		String path = getFullFilePath("actionVerbTable.txt");
		List<String> lines = TestDataProvider.readLines(path);
		lines.forEach((t) -> processActionVerbLine(t));
	}
	
	private void processLinkingModalVerbLine(String line){
		String[] contents = line.split(",");
		String token = contents[0];
		int size = contents.length;
		if(verbProcessingInput.getLinkingModalVerbMap().containsKey(token))return;	
		String state = null;
		if(size==4)
			state = getString(contents[3]);
		LinkingModalVerbItem item = linkingModalVerbItemFactory.create(getString(contents[1]),getString(contents[2]), contents[0],state);
		verbProcessingInput.getLinkingModalVerbMap().put(token, item);
	}
	
	private String getString(String val){
		if(val==null) return null; 
		if(val.equals("")) return null;
		return val;
	}
	
	private void processActionVerbLine(String line){
		String[] contents = line.split(",");
		
		String verbNetClass = null;
		if(contents.length==5)
			verbNetClass = contents[4];

		ActionVerbItem infinitivePresentItem = createActionVerbItem(VerbTense.InfinitivePresent, null,contents[0], verbNetClass);
		UUID infinitivePresentItemId = infinitivePresentItem.getId();
		acttionVerbTable.addValue(infinitivePresentItem);
	
		acttionVerbTable.addValue(createActionVerbItem(VerbTense.PluralInfinitivePresent,infinitivePresentItemId, contents[1],verbNetClass));
		acttionVerbTable.addValue(createActionVerbItem(VerbTense.Past,infinitivePresentItemId, contents[2],verbNetClass));
		acttionVerbTable.addValue(createActionVerbItem(VerbTense.Present,infinitivePresentItemId, contents[3],verbNetClass));
	}
	
	private ActionVerbItem createActionVerbItem(VerbTense tense, UUID infinitivePresentId, String verb, String verbNetClass){
		ActionVerbItem item = new ActionVerbItem();
		if(infinitivePresentId ==null){
			UUID id = UUID.randomUUID();
			infinitivePresentId =  id;
			item.setId(id);
		}
		else 
			item.setId( UUID.randomUUID());
		
		item.setInfinitivePresentId(infinitivePresentId);
		
		item.setVerbNetClass(verbNetClass);
		item.setVerb(verb);
		item.setVerbTense(tense);
		return item;
	}
}
