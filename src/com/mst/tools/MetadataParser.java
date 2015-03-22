package com.mst.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.mst.model.DependentPhraseMetadata;
import com.mst.model.NounPhraseMetadata;
import com.mst.model.PrepPhraseMetadata;
import com.mst.model.PrepPhraseToken;
import com.mst.model.Sentence;
import com.mst.model.SentenceMetadata;
import com.mst.model.GenericToken;
import com.mst.model.VerbPhraseToken;
import com.mst.model.VerbPhraseMetadata;
import com.mst.model.WordToken;
import com.mst.util.Constants;
import com.mst.util.Utils;

public class MetadataParser {

	private final Pattern datePattern = Pattern.compile("\\d\\d?/\\d\\d?/\\d{4}");
	//private final Pattern numericPattern = Pattern.compile("\\b\\d+\\b");
	public final Pattern specialPattern = Pattern.compile("\\$|%|\\*|#|@|_|\\+|&|<|=|>");
	public final Pattern hyphenPattern = Pattern.compile("-|�|�");
	public final Pattern uppercasePattern = Pattern.compile("[A-Z]{2,}");
	// regex that is numeric but also not part of a date
	// digit not bookended by / but allowing decimal and possibly preceded by a digit (eg. T11)
	public final Pattern numericPattern = Pattern.compile("(?<!\\/)\\b[A-Z]*\\d+\\.*\\d*\\b(?!\\/)");
	
	public void parseComplex(Sentence sentence) {
		ArrayList<WordToken> words = sentence.getWordList();
		SentenceMetadata metadata = sentence.getMetadata();
		//int verbCount = 0;
		boolean compoundVerb = false;
		int i = 0;
		
		if(containsPattern(datePattern, sentence.getFullSentence())) {
			metadata.addSimpleMetadataValue("containsDate", true);
		}
		if(containsPattern(numericPattern, sentence.getFullSentence())) {
			metadata.addSimpleMetadataValue("containsNumeric", true);
		}
		if(containsPattern(specialPattern, sentence.getFullSentence())) {
			metadata.addSimpleMetadataValue("containsSpecialChar", true);
		}
		if(containsPattern(hyphenPattern, sentence.getFullSentence())) {
			metadata.addSimpleMetadataValue("containsHyphen", true);
		}
		if(containsPattern(uppercasePattern, sentence.getFullSentence())) {
			metadata.addSimpleMetadataValue("containsUppercase", true);
		}
		
		for(WordToken word : words) {
			try {
				if(i == 0) {
					if(word.isPrepPhraseMember()) {
						metadata.addSimpleMetadataValue("beginsWithPreposition", true);
					}			
				}
				
				//if(word.isConjunction() && verbCount > 0) {
				//	compoundVerb = true;
				//}
				
				/* PREP phrases */
				if(word.isPrepPhraseObject() && !word.isPrepPhraseMember()) {
					PrepPhraseMetadata pp = new PrepPhraseMetadata();
					
					// reached the final token of the prep phrase
					pp.setPhrase(buildPrepPhrase(words, i));
						
					// check negation, beginning with position of prep phrase start
					pp.setNegated(checkNegation(words, pp.getPhrase().get(0).getPosition()));
					
					metadata.addPrepMetadata(pp);
				}

				
				/* VERB phrases (not handled in VerbPhraseHelper) */
				if(word.isInfinitiveVerb()) {
					VerbPhraseMetadata vp = new VerbPhraseMetadata(Constants.VerbClass.INFINITIVE);
					vp.setVerb(new VerbPhraseToken(word.getToken(), i));
					
					// TODO negated
					
					// infinitive follows prep phrase
					try {
						if(i > 0 && words.get(i-1).isPrepPhraseObject())
							vp.setInfFollowsPP(true);
					} catch(IndexOutOfBoundsException e) { }
					
					metadata.addVerbMetadata(vp);
				}	
					
				if(word.isPrepositionalVerb()) {
					VerbPhraseMetadata vp = new VerbPhraseMetadata(Constants.VerbClass.PREPOSITIONAL);
					vp.setVerb(new VerbPhraseToken(word.getToken(), i));
					
					// TODO negated?
									
					metadata.addVerbMetadata(vp);
				} 
				
				if(word.isModalAuxVerb()) {
					VerbPhraseMetadata vp = new VerbPhraseMetadata(Constants.VerbClass.MODAL_AUX);
					
					vp.setVerb(new VerbPhraseToken(word.getToken(), i));
					
					// a bit of a hack. set the subject of a modal aux verb as the term preceding the verb
					// modal aux are short phrases such as "could be" or "may have"
					try {
						vp.setSubj(new VerbPhraseToken(words.get(i-1).getToken(), i-1));
					} catch(Exception e) { }
					
					// TODO negated?
					
					metadata.addVerbMetadata(vp);
				} 
				
				
				/* NOUN phrases */
				if(word.isNounPhraseHead()) {
					NounPhraseMetadata np = new NounPhraseMetadata();
					// noun phrase HEAD = prep phrase OBJ
					if(word.isPrepPhraseObject()) {
						np.setWithinPP(true);
					}
					// noun phrase
					np.setPhrase(buildNounPhrase(words, i));
					
					// check negation, from start of phrase
					np.setNegated(checkNegation(words, np.getPhrase().get(0).getPosition()));
					
					// contains verb, backwards from head
					//nounPhraseContainsVerb(words, i);
					
					metadata.addNounMetadata(np);
				}
		
				
				/* DEPENDENT phrases */
				if(word.isDependentPhraseBegin()) {
					DependentPhraseMetadata dp = new DependentPhraseMetadata(word.getDependentPhraseBegin());

					// dependent phrase
					dp.setPhrase(buildDependentPhrase(words, i));

					metadata.addDependentMetadata(dp);
				}
				
				i++;
				
			} catch(Exception e) { 
				e.printStackTrace(); 
			}
		}
		
		// #####
		// loop through word tokens to build phrases complete
		// now loop through phrases built in previous loop and construct additional metadata
		// #####
		
		// compound verb phrases
//		if(compoundVerb) {
//			for(VerbPhraseMetadata phrase : metadata.getVerbMetadata())
//				// set every verb phrase compound = true
//				phrase.setCompound(true);
//		}
		
		for(NounPhraseMetadata phrase : metadata.getNounMetadata()) {
			int finalTokenPos = 0;
			for(GenericToken tp : phrase.getPhrase()) {
				// contains comma
				if(tp.getToken().equals(",")) {
					metadata.addSimpleMetadataValue("NPContainsComma", true);
				}
				finalTokenPos = tp.getPosition();
			}
			phrase.setPrepPhrasesIdx(getModifyingPrepPhrases(finalTokenPos, metadata.getPrepMetadata()));
		}
		
		for(PrepPhraseMetadata phrase : metadata.getPrepMetadata()) {
			for(PrepPhraseToken tp : phrase.getPhrase()) {
				tp.setNounPhraseIdx(getContainingNounPhraseIdx(tp.getPosition(), metadata.getNounMetadata()));
			}
		}
		
		for(VerbPhraseMetadata phrase : metadata.getVerbMetadata()) {
			if(phrase.getVerbClass() == Constants.VerbClass.ACTION ||
				phrase.getVerbClass() == Constants.VerbClass.LINKING_VERB ||
				phrase.getVerbClass() == Constants.VerbClass.VERB_OF_BEING) {
				
				phrase.getVerb().setNegated(checkNegation(words, phrase.getVerb().getPosition()));
				
				if(phrase.getSubj() != null) {
					phrase.getSubj().setPrepPhrasesIdx(getModifyingPrepPhrases(phrase.getSubj().getPosition(), metadata.getPrepMetadata()));
					phrase.getSubj().setNounPhraseIdx(getContainingNounPhraseIdx(phrase.getSubj().getPosition(), metadata.getNounMetadata()));
					phrase.getSubj().setNegated(checkSubjNegation(words, phrase.getSubj().getPosition()));
				}
				
				if(phrase.getSubjC() != null) {
					phrase.getSubjC().setPrepPhrasesIdx(getModifyingPrepPhrases(phrase.getSubjC().getPosition(), metadata.getPrepMetadata()));
					phrase.getSubjC().setNounPhraseIdx(getContainingNounPhraseIdx(phrase.getSubjC().getPosition(), metadata.getNounMetadata()));
					phrase.getSubjC().setNegated(checkSubjCNegation(words, phrase.getSubjC().getPosition()));
					
					phrase.getSubjC().setDepPhraseIdx(findModifyingDependentPhrase(words, phrase.getSubjC().getPosition(), metadata.getDependentMetadata()));
				}
				
			} else if(phrase.getVerbClass() == Constants.VerbClass.PREPOSITIONAL) {
				phrase.getVerb().setPrepPhrasesIdx(getModifyingPrepPhrases(phrase.getVerb().getPosition(), metadata.getPrepMetadata()));
			}
		}
		
		for(DependentPhraseMetadata phrase : metadata.getDependentMetadata()) {
			phrase.setPrepPhrasesIdx(getModifyingPrepPhrases(phrase.getPhrase().get(phrase.getPhrase().size()-1).getPosition(), metadata.getPrepMetadata()));
		}
	}
	
	private int findModifyingDependentPhrase(ArrayList<WordToken> words, int subjcPos, List<DependentPhraseMetadata> dpMetadata) {
		int depPhrasePos = -1;
		
		// TODO kill this with fire and make it not suck
		
		int startPos = subjcPos;
		
		if(words.get(startPos).isDependentPhraseMember()) {
			do {
				startPos++;
			} while(words.get(startPos).isDependentPhraseMember());
		}
		
		// loop forward from subj complement position until a dependent phrase member is found
		for(int i=startPos; i < words.size(); i++) {
			if(words.get(i).isDependentPhraseMember()) {
				int phraseStartIdx = i;
				int phraseEndIdx = -1;
				int dpIdx = 0;
				// find the dependent phrase object in the list
				for(DependentPhraseMetadata phrase : dpMetadata) {
					if(phrase.getPhrase().get(0).getPosition() == phraseStartIdx) {
						phraseEndIdx = phrase.getPhrase().get(phrase.getPhrase().size()-1).getPosition();
						break;
					}
					dpIdx++;
				}
				
				boolean vobSubj = false, vobSubjC = false, vob = false;
				boolean lvSubj = false, lvSubjC = false, lv = false;
				boolean avSubj = false, avSubjC = false, av = false;
				
				for(int j=phraseStartIdx; j <= phraseEndIdx; j++) {
					if(words.get(j).isActionVerb())
						av = true;
					if(words.get(j).isActionVerbSubject())
						avSubj = true;
					if(words.get(j).isActionVerbDirectObject())
						avSubjC = true;
					
					if(words.get(j).isLinkingVerb())
						lv = true;
					if(words.get(j).isLinkingVerbSubject())
						lvSubj = true;
					if(words.get(j).isLinkingVerbSubjectComplement())
						lvSubjC = true;
					
					if(words.get(j).isVerbOfBeing())
						vob = true;
					if(words.get(j).isVerbOfBeingSubject())
						vobSubj = true;
					if(words.get(j).isVerbOfBeingSubjectComplement())
						vobSubjC = true;
				}
				
				// the idea is to only process if a complete verb structure is present within a DP
				// each type of verb structure has to be tracked independently because, for example, you could have a verb/obj from an AV and a subj from an LV
				// in the same sentence. this still isn't perfect because there could be two of the same class of verb phrase in the same damn DP.
				
				// do something if none of the verb phrases are complete.
				if(!((vobSubj && vobSubjC && vob) || (lvSubj && lvSubjC && lv) || (avSubj && avSubjC && av))) {
					depPhrasePos = dpIdx;
				}
				
				break;
			}
		}
		
		return depPhrasePos;
	}
	
	private VerbPhraseToken findSubject(ArrayList<WordToken> words, int verbPos, Constants.VerbClass _class) {
		VerbPhraseToken tpos = null;
		
		for(int i=verbPos-1; i >= 0; i--) {
			switch(_class) {
				case ACTION:
					if(words.get(i).isActionVerbSubject()) {
						tpos = new VerbPhraseToken(words.get(i).getToken(), i);
					}
					break;
				case VERB_OF_BEING:
					if(words.get(i).isVerbOfBeingSubject()) {
						tpos = new VerbPhraseToken(words.get(i).getToken(), i);
					}
					break;
				case LINKING_VERB:
					if(words.get(i).isLinkingVerbSubject()) {
						tpos = new VerbPhraseToken(words.get(i).getToken(), i);
					}
					break;
				default:
					
					break;
			}
			if(tpos != null)
				break;
		}
		
		return tpos;
	}
	
	private VerbPhraseToken findObjectOrSubjectComplement(ArrayList<WordToken> words, int verbPos, Constants.VerbClass _class) {
		VerbPhraseToken tpos = null;
		
		for(int i=verbPos+1; i < words.size(); i++) {
			switch(_class) {
				case ACTION:
					if(words.get(i).isActionVerbDirectObject()) {
						tpos = new VerbPhraseToken(words.get(i).getToken(), i);
					}
					break;
				case VERB_OF_BEING:
					if(words.get(i).isVerbOfBeingSubjectComplement()) {
						tpos = new VerbPhraseToken(words.get(i).getToken(), i);
					}
					break;
				case LINKING_VERB:
					if(words.get(i).isLinkingVerbSubjectComplement()) {
						tpos = new VerbPhraseToken(words.get(i).getToken(), i);
					}
					break;
				default:
					
					break;
			}
			if(tpos != null)
				break;
		}
		
		return tpos;
	}
	
	private boolean checkNegation(ArrayList<WordToken> words, int tokenPos) {
		boolean ret = false;
		
		try {	
			if(words.get(tokenPos-1).isNegationToken()) {
				ret = true;
			} else if(words.get(tokenPos-1).isArticle() && words.get(tokenPos-2).isNegationToken()) {
				ret = true;
			}
		} catch(IndexOutOfBoundsException oob) { }
		
		return ret;
	}
	
	private boolean checkSubjCNegation(ArrayList<WordToken> words, int tokenPos) {
		boolean ret = false;
		
		try {
			// loop backwards from subject complement to verb
			for(int i=tokenPos-1; i >= 0; i--) {
				if(words.get(i).isVerbOfBeing() || words.get(i).isLinkingVerb()) {
					break;
				} else if(words.get(i).isNegationToken()) {
					ret = true;
					break;
				}
			}
		} catch(IndexOutOfBoundsException oob) { }
		
		return ret;
	}
	
	private boolean checkSubjNegation(ArrayList<WordToken> words, int tokenPos) {
		boolean ret = false;
		
		try {
			// loop backwards from subject to start of sentence
			for(int i=tokenPos-1; i >= 0; i--) {
				if(words.get(i).isNegationToken()) {
					ret = true;
					break;
				}
			}
		} catch(IndexOutOfBoundsException oob) { }
		
		return ret;
	}
	
	private boolean isWithinDependentPhrase(ArrayList<WordToken> words, int tokenPos) {
		return words.get(tokenPos).isDependentPhraseMember() || words.get(tokenPos).isDependentPhraseBegin();
	}
	
	private boolean containsPattern(Pattern pattern, String fullSentence) {
		Matcher matcher = pattern.matcher(fullSentence);
		return matcher.find();
	}
	
	private GenericToken isTokenModifiedByPrepPhrase(ArrayList<WordToken> words, int tokenPos) {
		GenericToken tpos = null;
		
		try {
			// this token is NOT a prep phrase member but the next token IS (the final token of a prep phrase is never a member)	
			if(!words.get(tokenPos).isPrepPhraseMember() && words.get(tokenPos+1).isPrepPhraseMember()) {
				// return object or preposition?
				tpos = new GenericToken(words.get(tokenPos+1).getToken(), tokenPos+1);
				/*
				for(int i=tokenPos+1; i < words.size(); i++) {
					if(words.get(i).isPrepPhraseObject()) {
						tpos = new TokenPosition(words.get(i).getToken(), i);
						break;
					}
				}
				*/
			}
		} catch(IndexOutOfBoundsException oob) { }
		
		return tpos;
	}
	
	private boolean isTokenNounPhraseHead(ArrayList<WordToken> words, int tokenPos) {
		return words.get(tokenPos).isNounPhraseHead();		
	}

	private List<GenericToken> getCompoundVerbs(ArrayList<WordToken> words, int startPos) {
		List<GenericToken> tposList = new ArrayList<GenericToken>();
		boolean conjFound = false;
		
		// add initial verb
		tposList.add(new GenericToken(words.get(startPos).getToken(), startPos));
		
		for(int i=startPos+1; i < words.size(); i++) {
			if(words.get(i).isConjunction()) {
				conjFound = true;
				tposList.add(new GenericToken(words.get(i).getToken(), i));
			} else {
				if(conjFound) {
					if(words.get(i).isLinkingVerb() || words.get(i).isInfinitiveVerb() || words.get(i).isVerbOfBeing()) {
						tposList.add(new GenericToken(words.get(i).getToken(), i));
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

	private List<GenericToken> buildNounPhrase(ArrayList<WordToken> words, int startPos) {
		List<GenericToken> tposList = new ArrayList<GenericToken>();
		
		// add initial noun phrase HEAD
		tposList.add(new GenericToken(words.get(startPos).getToken(), startPos));
		
		for(int i=startPos-1; i >= 0; i--) {
			if(words.get(i).isNounPhraseModifier()) {
				tposList.add(new GenericToken(words.get(i).getToken(), i));
			} else {
				break;
			}
		}
		
		if(tposList.isEmpty())
			tposList = null;
		else
			tposList = Lists.reverse(tposList);
		
		return tposList;
	}
	
	// for error-checking purposes. may remove in time.
//	private boolean nounPhraseContainsVerb(ArrayList<WordToken> words, int startPos) {
//		boolean ret = false;
//		
//		for(int i=startPos-1; i >= 0; i--) {
//			if(words.get(i).isNounPhraseModifier()) {
//				if(words.get(i).isVerb()) {
//					ret = true;
//					break;
//				}
//			} else {
//				break;
//			}
//		}
//		
//		return ret;
//	}
	
	private List<PrepPhraseToken> buildPrepPhrase(ArrayList<WordToken> words, int startPos) {
		List<PrepPhraseToken> tposList = new ArrayList<PrepPhraseToken>();
		
		//boolean inNP = (words.get(startPos).isNounPhraseHead() || words.get(startPos).isNounPhraseModifier()); 
		//int npIdx = getContainingNounPhraseIdx(startPos, nounList); 
		
		// add prep phrase object
		tposList.add(new PrepPhraseToken(words.get(startPos).getToken(), startPos, -1));
		
		for(int i=startPos-1; i >= 0; i--) {
			if(words.get(i).isPrepPhraseMember()) {
				//npIdx = getContainingNounPhraseIdx(startPos, nounList); 
				tposList.add(new PrepPhraseToken(words.get(i).getToken(), i, -1));
			} else {
				break;
			}
		}
		
		if(tposList.isEmpty())
			tposList = null;
		else
			tposList = Lists.reverse(tposList);
		
		return tposList;
	}
	
	private List<GenericToken> buildDependentPhrase(ArrayList<WordToken> words, int startPos) {
		List<GenericToken> tposList = new ArrayList<GenericToken>();
		
		for(int i=startPos; i < words.size(); i++) {
			tposList.add(new GenericToken(words.get(i).getToken(), i));
			
			if(words.get(i).isDependentPhraseEnd())
				break;
		}
		
		if(tposList.isEmpty())
			tposList = null;
		
		return tposList;
	}

//	private List<PrepPhraseMetadata> getModifyingPrepPhrases(int sourcePos, List<PrepPhraseMetadata> prepList) {
//		List<PrepPhraseMetadata> modList = new ArrayList<PrepPhraseMetadata>();
//		
//		// loop through each prep phrase and add it to a list if it modifies the token before it
//		for(PrepPhraseMetadata phrase : prepList) {
//			if(phrase.getPhrase().get(0).getPosition() == sourcePos+1) {
//				modList.add(phrase);
//				sourcePos = phrase.getPhrase().get(phrase.getPhrase().size()-1).getPosition();
//			}
//		}
//		
//		return modList;
//	}
	
	private List<Integer> getModifyingPrepPhrases(int sourcePos, List<PrepPhraseMetadata> prepList) {
		List<Integer> modList = new ArrayList<Integer>();
		
		// loop through each prep phrase and add it to a list if it modifies the token before it
		for(int i=0; i < prepList.size(); i++) {
			PrepPhraseMetadata phrase = prepList.get(i);
			if(phrase.getPhrase().get(0).getPosition() == sourcePos+1) {
				modList.add(i);
				sourcePos = phrase.getPhrase().get(phrase.getPhrase().size()-1).getPosition();
			}
		}
		
		return modList;
	}
	
	private NounPhraseMetadata getContainingNounPhrase(int sourcePos, List<NounPhraseMetadata> nounList) {
		NounPhraseMetadata npMetadata = null;
		
		for(NounPhraseMetadata phrase : nounList) {
			for(GenericToken token : phrase.getPhrase()) {
				if(token.getPosition() == sourcePos) {
					npMetadata = phrase;
					break;
				}
			}
			if(npMetadata != null)
				break;
		}
		
		return npMetadata;
	}
	
	private int getContainingNounPhraseIdx(int sourcePos, List<NounPhraseMetadata> nounList) {
		int idx = -1;
		
		for(int i=0; i < nounList.size(); i++) {
			NounPhraseMetadata phrase = nounList.get(i);
			for(GenericToken token : phrase.getPhrase()) {
				if(token.getPosition() == sourcePos) {
					idx = i;
					break;
				}
			}
			if(idx != -1)
				break;
		}
		
		return idx;
	}
}
