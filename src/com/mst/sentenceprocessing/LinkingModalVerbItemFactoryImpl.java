package com.mst.sentenceprocessing;

import com.mst.interfaces.LinkingModalVerbItemFactory;
import com.mst.model.sentenceProcessing.LinkingModalVerbItem;
import com.mst.model.sentenceProcessing.VerbTense;
import com.mst.model.sentenceProcessing.VerbType;

public class LinkingModalVerbItemFactoryImpl implements LinkingModalVerbItemFactory {

 
	public LinkingModalVerbItem create(String verbType, String verbTense, String token, String state) {
		
		LinkingModalVerbItem item = new LinkingModalVerbItem();
		item.setToken(token);
		item.setVerbState(state);
		
		if(verbType.equals("lv"))
			item.setVerbType(VerbType.LV);
		else if (verbType.equals("mv"))
			item.setVerbType(VerbType.MV);
		
		VerbTense tense= getVerbTense(verbTense);
		if(tense!=null)
			item.setVerbTense(tense);
		return item;
	}
	
	private VerbTense getVerbTense(String verbTense){
		VerbTense tense=null; 
		if(verbTense==null) return null;
		
		if(verbTense.toLowerCase().equals("present"))
			tense = VerbTense.Present;
		else if(verbTense.toLowerCase().equals("past"))
			tense = VerbTense.Past;
		else if(verbTense.toLowerCase().equals("future"))
			tense = VerbTense.Future;
		return tense;

	}

}
