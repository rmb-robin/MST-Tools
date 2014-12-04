package com.mst.tools;

import java.util.ArrayList;
import java.util.List;

import com.mst.model.Sentence;
import com.mst.model.SentenceMetadata;
import com.mst.model.TokenPosition;
import com.mst.model.VerbPhraseMetadata;
import com.mst.model.WordToken;
import com.mst.util.Constants;

public class MetadataParser {

	public void parseComplex(Sentence sentence) {
		ArrayList<WordToken> words = sentence.getWordList();
		SentenceMetadata metadata = sentence.getSentenceMetadata();
		
		//if(metadata.containsNounPhrase() && metadata.containsPrepPhrase()) {
			for(int i=0; i < words.size(); i++) {
				try {
					if(words.get(i).isPrepPhraseObject()) {
						// if noun phrase within prep phrase
						if(words.get(i).isNounPhraseHead()) {
							metadata.addNounPhraseInPrepPhrase(new TokenPosition(words.get(i).getToken(), i));
						}
						
						// prep OBJ followed by infinitive verb
						if(words.get(i+1).isInfinitiveHead()) {
							metadata.addInfVerbFollowsPrepPhrase(new TokenPosition(words.get(i+1).getToken(), i+1));
						}
					}
					
					// TODO it feels like this processing could happen in VerbHelper.java
					if(words.get(i).isVerbOfBeing() || words.get(i).isLinkingVerb()) {
						VerbPhraseMetadata vmetadata = new VerbPhraseMetadata(words.get(i).isVerbOfBeing() ? Constants.VerbClass.VERB_OF_BEING : Constants.VerbClass.LINKING_VERB);
						vmetadata.setVerb(new TokenPosition(words.get(i).getToken(), i));
						vmetadata.setSubj(findSubject(words, i));
						vmetadata.setSubjC(findSubjectComplement(words, i));
						vmetadata.setSubjModByPP(isTokenModifiedByPrepPhrase(words, vmetadata.getSubj().getPosition()));
						vmetadata.setSubjCModByPP(isTokenModifiedByPrepPhrase(words, vmetadata.getSubjC().getPosition()));
						vmetadata.setSubjEqNPHead(isTokenNounPhraseHead(words, vmetadata.getSubj().getPosition()));
						vmetadata.setSubjCEqNPHead(isTokenNounPhraseHead(words, vmetadata.getSubjC().getPosition()));
						// TODO negated
						metadata.addVerbMetadata(vmetadata);
					} else if(words.get(i).isInfinitive()) {
						VerbPhraseMetadata vmetadata = new VerbPhraseMetadata(Constants.VerbClass.INFINITIVE);
						vmetadata.setVerb(new TokenPosition(words.get(i).getToken(), i));
						// TODO negated
						metadata.addVerbMetadata(vmetadata);
					}
					
				
					
				} catch(IndexOutOfBoundsException oob) { }
			}
			
			// compound verb phrases
			// only if more than one verb in the sentence
			if(metadata.getVerbMetadata().size() > 1) {
				// start with the first verb object's position
				metadata.setCompoundVerbPhrases(getCompoundVerbs(words, metadata.getVerbMetadata().get(0).getVerb().getPosition()));
			}
		//}
	}
	
	private TokenPosition findSubject(ArrayList<WordToken> words, int verbPos) {
		TokenPosition tpos = null;
		
		for(int i=verbPos+1; i > 0; i--) {
			if(words.get(i).isVerbOfBeingSubject() || words.get(i).isLinkingVerbSubject()) {
				tpos = new TokenPosition(words.get(i).getToken(), i);
				break;
			}
		}
		
		return tpos;
	}
	
	private TokenPosition findSubjectComplement(ArrayList<WordToken> words, int verbPos) {
		TokenPosition tpos = null;
		
		for(int i=verbPos+1; i < words.size(); i++) {
			if(words.get(i).isVerbOfBeingSubjectComplement() || words.get(i).isLinkingVerbSubjectComplement()) {
				tpos = new TokenPosition(words.get(i).getToken(), i);
				break;
			}
		}
		
		return tpos;
	}
	
	private TokenPosition isTokenModifiedByPrepPhrase(ArrayList<WordToken> words, int tokenPos) {
		TokenPosition tpos = null;
		
		// this token is NOT part of a prep phrase but next token IS 
		// TODO need to handle OOB on index+1?
		if(!words.get(tokenPos).isPrepPhraseMember() && words.get(tokenPos+1).isPrepPhraseMember()) {
			for(int i=tokenPos+1; i < words.size(); i++) {
				if(words.get(i).isPrepPhraseObject()) {
					tpos = new TokenPosition(words.get(i).getToken(), i);
					break;
				}
			}
		}
		
		return tpos;
	}
	
	private boolean isTokenNounPhraseHead(ArrayList<WordToken> words, int tokenPos) {
		return words.get(tokenPos).isNounPhraseHead();		
	}

	private List<TokenPosition> getCompoundVerbs(ArrayList<WordToken> words, int startPos) {
		List<TokenPosition> tposList = new ArrayList<TokenPosition>();
		boolean conjFound = false;
		
		// add initial verb
		tposList.add(new TokenPosition(words.get(startPos).getToken(), startPos));
		
		for(int i=startPos+1; i < words.size(); i++) {
			if(words.get(i).isConjunction()) {
				conjFound = true;
				tposList.add(new TokenPosition(words.get(i).getToken(), i));
			} else {
				if(conjFound) {
					if(words.get(i).isLinkingVerb() || words.get(i).isInfinitive() || words.get(i).isVerbOfBeing()) {
						tposList.add(new TokenPosition(words.get(i).getToken(), i));
						conjFound = false;
					}
				}
			}
			
		}
		
		// don't return empty, single-verb, or verb/conj-only list
		if(tposList.size() <= 2)
			tposList = null;
		
		return tposList;
	}
}
