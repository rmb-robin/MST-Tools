package com.mst.sentenceprocessing;

import java.util.List;
import java.util.UUID;

import com.mst.interfaces.sentenceprocessing.VerbProcessor;
import com.mst.model.metadataTypes.VerbTense;
import com.mst.model.metadataTypes.VerbType;
import com.mst.model.sentenceProcessing.ActionVerbItem;
import com.mst.model.sentenceProcessing.ActionVerbTable;
import com.mst.model.sentenceProcessing.LinkingModalVerbItem;
import com.mst.model.sentenceProcessing.Verb;
import com.mst.model.sentenceProcessing.VerbProcessingInput;
import com.mst.model.sentenceProcessing.WordToken;

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
		
		boolean isExistance = false;
		if(item.getVerbType().equals(VerbType.LV))
			isExistance = true;
		
		Verb verb = createVerb(item.getVerbTense(),item.getVerbType(),item.getVerbState(),null,isExistance,false,false,null);
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
		UUID presentId = item.getInfinitivePresentId();
		ActionVerbItem presentVerb = actionVerbTable.getVerbyById().get(presentId);
		String presentVerbName = null;
		if(presentVerb!=null)
			presentVerbName = presentVerb.getVerb();
		
		Verb verb = createVerb(item.getVerbTense(), VerbType.AV,null,item.getVerbNetClass(), item.getIsExistance(),item.getIsMaintainVerbNetClass(),item.getIsNegation(),presentVerbName);
		wordToken.setVerb(verb);
		
		if(item.getId()== item.getInfinitivePresentId()) return true;
			
		if(actionVerbTable.getVerbyById().containsKey(item.getInfinitivePresentId())){
			item = actionVerbTable.getVerbyById().get(item.getInfinitivePresentId());
			wordToken.setToken(item.getVerb());
		}
		return true;
	}
	
	private Verb createVerb(VerbTense verbTense, VerbType verbType, String state, String verbNetClass, boolean isExistance,boolean isMaintainVerbNetClass,boolean isNegation,String presentTerm){
		Verb verb = new Verb();
		verb.setVerbTense(verbTense);
		verb.setVerbType(verbType);
		verb.setVerbState(state);
		verb.setVerbNetClass(verbNetClass);
		verb.setExistance(isExistance);
		verb.setMaintainVerbNetClass(isMaintainVerbNetClass);
		verb.setNegation(isNegation);
		verb.setPresentVerb(presentTerm);
		return verb;
	}


}
