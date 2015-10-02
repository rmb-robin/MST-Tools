package com.mst.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.mst.util.Constants;

// The purpose for metadata, in general, is to ease downstream processing when creating the structured output (JSON that represents Ontology/Snomed mapping).
// The idea is to choose different processing paths based on features of the sentence, such as (really bad example)...
//   "Does the sentence start with a preposition and contain more than one verb?" 

public class SentenceMetadata {
	// simple - aggregate data related to the sentence as a whole
	// more simple metadata values could be added if a decision is made that storing here is preferable to computing later
	// ex1. boolean nounPhrasePresent vs. nounPhrases.size() > 1
	// ex2. boolean nounPhraseInPrepPhrase vs. for(NounPhraseMetadata npm : nounPhrases) { if(npm.isHeadEqPPObj()) return true; }
	//private boolean beginsWithPrep;
	private Map<String, Object> simple = new HashMap<String, Object>();
	
	// complex - inter and intra phrase-level data that is more involved than a simple count or boolean 
	private List<VerbPhraseMetadata> verbPhrases = new ArrayList<VerbPhraseMetadata>(); 
	private List<NounPhraseMetadata> nounPhrases = new ArrayList<NounPhraseMetadata>();
	private List<PrepPhraseMetadata> prepPhrases = new ArrayList<PrepPhraseMetadata>();
	private List<DependentPhraseMetadata> dependentPhrases = new ArrayList<DependentPhraseMetadata>();
		
	
	public int addVerbMetadata(VerbPhraseMetadata val) {
		// return the index of the phrase just added
		verbPhrases.add(val);
		return verbPhrases.size()-1;
	}
	
	public List<VerbPhraseMetadata> getVerbMetadata() { return verbPhrases; }
	
	public boolean addNounMetadata(NounPhraseMetadata val) { return nounPhrases.add(val); }
	public List<NounPhraseMetadata> getNounMetadata() { return nounPhrases; }
	
	public boolean addPrepMetadata(PrepPhraseMetadata val) { return prepPhrases.add(val); }
	public List<PrepPhraseMetadata> getPrepMetadata() { return prepPhrases; }
	
	public boolean addDependentMetadata(DependentPhraseMetadata val) { return dependentPhrases.add(val); }
	public List<DependentPhraseMetadata> getDependentMetadata() { return dependentPhrases; }
	
	//public void setBeginsWithPreposition(boolean val) { beginsWithPrep = val; };
	//public boolean beginsWithPreposition() { return beginsWithPrep; };

	public Map<String, Object> getSimpleMetadata() {
		return this.simple;
	}
	
	public Object getSimpleMetadataValue(String key) {
		return this.simple.get(key);
	}
	
	public boolean addSimpleMetadataValue(String key, Object value) {
		boolean ret = true;
		try {
			simple.put(key, value);
		} catch(Exception e) {
			ret = false;
		}
		return ret;
	}
	
	public boolean removeSimpleMetadataValue(String key) {
		boolean ret = true;
		try {
			simple.remove(key);
		} catch(Exception e) {
			ret = false;
		}
		return ret;
	}
	
	public String getAnnotatedMarkup(ArrayList<WordToken> words) {
		ArrayList<String> markup = new ArrayList<String>();
		
		String vobOpen = "<vob>", vobClose = "</vob>";
		String lvOpen = "<lv>", lvClose = "</lv>"; // linking verb
		String mvOpen = "<mv>", mvClose = "</mv>"; // modal aux
		String avOpen = "<av>", avClose = "</av>"; // action verb
		String ivOpen = "<iv>", ivClose = "</iv>"; // infinitive verb
		String prepOpen = "<pp>", prepClose = "</pp>"; // prepositional phrase
		String prepObj = prepOpen + "/OBJ" + prepClose; // prepositional phrase object
		String prepOpenBracket = prepOpen + "[" + prepClose, prepCloseBracket = prepOpen + "]" + prepClose;
		String nounOpen = "<np>[</np>", nounClose = "<np>]</np>"; // noun phrase
		String dpOpen = "<dp>", dpClose = "</dp>"; // dependent phrase
		String stOpen = "<st><sup>", stClose = "</sup></st>"; // semantic type
		String posOpen = "<pos>/", posClose = "</pos>"; // part-of-speech
		String subj = "/SUBJ";
		String subjc = "/SUBJC";

		for(WordToken word : words) {
			StringBuilder sb = new StringBuilder();
			sb.append(word.getToken())
			  .append(posOpen).append(word.getPOS()).append(posClose);
			if(word.getSemanticType() != null)
			  sb.append(stOpen).append(word.getSemanticType()).append(stClose);
			markup.add(sb.toString());
		}
		
		for(VerbPhraseMetadata vp : verbPhrases) {
			String openTag = "", closeTag = "";
			
			switch(vp.getVerbClass()) {
				case VERB_OF_BEING:
					openTag = vobOpen;
					closeTag =vobClose;
					break;
				case LINKING_VERB:
					openTag = lvOpen;
					closeTag =lvClose;
					break;
				case MODAL_AUX:
					openTag = mvOpen;
					closeTag = mvClose;
					break;
				case ACTION:
					openTag = avOpen;
					closeTag =avClose;
					break;
				case PREPOSITIONAL:
					openTag = prepOpen;
					closeTag =prepClose;
					break;
				case INFINITIVE:
					openTag = ivOpen;
					closeTag =ivClose;
					break;
			}
			
			int idx = 0;
			
			if(vp.getSubj() != null) {
				idx = vp.getSubj().getPosition();
				markup.set(idx, markup.get(idx) + openTag + subj + closeTag);
			}
			
			for(VerbPhraseToken token : vp.getSubjC()) {
				idx = token.getPosition();
				markup.set(idx, markup.get(idx) + openTag + subjc + closeTag);
			}
			
			for(VerbPhraseToken token : vp.getVerbs()) {
				idx = token.getPosition();
				markup.set(idx, openTag + "[" + closeTag + markup.get(idx) + openTag + "]" + closeTag);
			}
		}
		
		for(PrepPhraseMetadata pp : prepPhrases) {
			int idx = pp.getPhrase().get(0).getPosition();
			markup.set(idx, prepOpenBracket + markup.get(idx));
			
			for(PrepPhraseToken ppt : pp.getPhrase()) {
				if(ppt.isObject())
					markup.set(ppt.getPosition(), markup.get(ppt.getPosition()) + prepObj);
			}
			
			idx = pp.getPhrase().get(pp.getPhrase().size()-1).getPosition();
			markup.set(idx, markup.get(idx) + prepCloseBracket);
		}
		
		for(NounPhraseMetadata np : nounPhrases) {
			int idx = np.getPhrase().get(0).getPosition();
			markup.set(idx, nounOpen + markup.get(idx));
			
			idx = np.getPhrase().get(np.getPhrase().size()-1).getPosition();
			markup.set(idx, markup.get(idx) + nounClose);
		}
		
		for(DependentPhraseMetadata dp : dependentPhrases) {
			int idx = dp.getPhrase().get(0).getPosition();
			markup.set(idx, dpOpen + markup.get(idx));
			
			idx = dp.getPhrase().get(dp.getPhrase().size()-1).getPosition();
			markup.set(idx, markup.get(idx) + dpClose);
		}
		
		return Joiner.on(" ").join(markup);
	}
}