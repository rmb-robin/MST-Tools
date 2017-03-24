package com.mst.sentenceprocessing;

import java.util.HashSet;
import java.util.List;

import com.mst.interfaces.VerbProcessor;
import com.mst.model.WordToken;
import com.mst.model.gentwo.ActionVerbItem;
import com.mst.model.gentwo.ActionVerbTable;
import com.mst.model.gentwo.Verb;
import com.mst.model.gentwo.VerbProcessingInput;
import com.mst.model.gentwo.VerbTense;
import com.mst.model.gentwo.VerbType;

public class VerbProcessorImpl implements VerbProcessor {

	private VerbProcessingInput verbProcessingInput;
	public List<WordToken> process(List<WordToken> wordTokens, VerbProcessingInput verbProcessingInput) throws Exception {
		if(verbProcessingInput==null) 
			throw new Exception("VerbProcessingInput is null");
			
		if(wordTokens==null)
			throw new Exception("wordTokens are null");
		
		this.verbProcessingInput = verbProcessingInput;
		wordTokens.forEach((a)-> processToken(a));
		return wordTokens;
	}
	
	private void processToken(WordToken wordToken){
	
		if(processForActionVerb(wordToken))return;
		
		//process for other vb ttypes .
	}
	
	private boolean processForActionVerb(WordToken wordToken){
		
		String key = wordToken.getToken().toLowerCase();
		ActionVerbTable actionVerbTable = verbProcessingInput.getActionVerbTable();
		if(!actionVerbTable.getVerbsByWord().containsKey(key))
			return false;
		
		ActionVerbItem item = actionVerbTable.getVerbsByWord().get(key);
		wordToken.setPos(VerbType.AV.toString());	
		Verb verb = createVerb(item.getVerbTense(), VerbType.AV);
		wordToken.setVerb(verb);
		
		if(item.getId()== item.getInfinitivePresentId()) return true;
			
		if(actionVerbTable.getVerbyById().containsKey(item.getInfinitivePresentId())){
			item = actionVerbTable.getVerbyById().get(item.getInfinitivePresentId());
			wordToken.setToken(item.getVerb());
		}
	
		return true;
		
		
	}
	
	private Verb createVerb(VerbTense verbTense, VerbType verbType){
		Verb verb = new Verb();
		verb.setVerbTense(verbTense);
		verb.setVerbType(verbType);
		return verb;
	}


}
