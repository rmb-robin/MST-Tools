package com.mst.sentenceprocessing;

import java.util.List;
import com.mst.interfaces.VerbProcessor;
import com.mst.model.gentwo.ActionVerbItem;
import com.mst.model.gentwo.ActionVerbTable;
import com.mst.model.gentwo.LinkingModalVerbItem;
import com.mst.model.gentwo.Verb;
import com.mst.model.gentwo.VerbProcessingInput;
import com.mst.model.gentwo.VerbTense;
import com.mst.model.gentwo.VerbType;
import com.mst.model.gentwo.WordToken;

public class VerbProcessorImpl implements VerbProcessor {

	private VerbProcessingInput verbProcessingInput;
	private List<WordToken> wordTokens;  
	public List<WordToken> process(List<WordToken> wordTokens, VerbProcessingInput verbProcessingInput) throws Exception {
		
		if(verbProcessingInput==null) 
			throw new Exception("VerbProcessingInput is null");
			
		if(wordTokens==null)
			throw new Exception("wordTokens are null");
		this.wordTokens = wordTokens;
		this.verbProcessingInput = verbProcessingInput;
		int index = 0;
		for(WordToken wordToken : wordTokens){
			processToken(wordToken,index);
			index+=1;
		}
		return wordTokens;
	}
	
	private void processToken(WordToken wordToken, int index){
		if(processForActionVerb(wordToken)){
			checkForInfinitivePhrase(index,wordToken);
			return;
		}
		
		if(processForLinkingModalVerbs(wordToken) && wordToken.getVerb().getVerbType()== VerbType.LV)
			checkForInfinitivePhrase(index,wordToken);
	}

	private void checkForInfinitivePhrase(int index, WordToken verbToken){
		if(index==0) return;
		String text = wordTokens.get(index-1).getToken().toLowerCase();
		if(!verbProcessingInput.getInfinitiveSignals().contains(text))return;
		WordToken token = wordTokens.get(index-1);
		token.setPos(verbProcessingInput.getInfinitiveSignalPosType());
		verbToken.setPos(VerbType.IV.toString());
		verbToken.getVerb().setVerbType(VerbType.IV);
	}
	
	private boolean processForLinkingModalVerbs(WordToken wordToken){
		String key = wordToken.getToken().toLowerCase();
		if(!verbProcessingInput.getLinkingModalVerbMap().containsKey(key))return false;
		
		LinkingModalVerbItem item = verbProcessingInput.getLinkingModalVerbMap().get(key);
		wordToken.setPos(item.getVerbType().toString());
		Verb verb = createVerb(item.getVerbTense(),item.getVerbType(),item.getVerbState(),null);
		wordToken.setVerb(verb);
		return true;
	}
	
	private boolean processForActionVerb(WordToken wordToken){
		
		String key = wordToken.getToken().toLowerCase();
		ActionVerbTable actionVerbTable = verbProcessingInput.getActionVerbTable();
		if(!actionVerbTable.getVerbsByWord().containsKey(key))
			return false;
		
		ActionVerbItem item = actionVerbTable.getVerbsByWord().get(key);
		wordToken.setPos(VerbType.AV.toString());	
		Verb verb = createVerb(item.getVerbTense(), VerbType.AV,null,item.getVerbNetClass());
		wordToken.setVerb(verb);
		
		if(item.getId()== item.getInfinitivePresentId()) return true;
			
		if(actionVerbTable.getVerbyById().containsKey(item.getInfinitivePresentId())){
			item = actionVerbTable.getVerbyById().get(item.getInfinitivePresentId());
			wordToken.setToken(item.getVerb());
		}
		return true;
	}
	
	private Verb createVerb(VerbTense verbTense, VerbType verbType, String state, String verbNetClass){
		Verb verb = new Verb();
		verb.setVerbTense(verbTense);
		verb.setVerbType(verbType);
		verb.setVerbState(state);
		verb.setVerbNetClass(verbNetClass);
		return verb;
	}


}
