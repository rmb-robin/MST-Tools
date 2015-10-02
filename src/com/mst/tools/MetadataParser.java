package com.mst.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redis.clients.jedis.Jedis;

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
		if(containsPattern(Constants.NEGATION, sentence.getFullSentence())) {
			metadata.addSimpleMetadataValue("containsNegation", true);
		}
		// TODO add containsMultipleVerbPhrases, possibly containsParens
		
		for(WordToken word : words) {
			try {
				if(i == 0) {
					if(word.isPrepPhraseMember()) {
						metadata.addSimpleMetadataValue("beginsWithPreposition", true);
					}			
				}
				
				/* PREP phrases */
				if(word.isPrepPhraseObject() && !word.isPrepPhraseMember()) {
					PrepPhraseMetadata pp = new PrepPhraseMetadata();
					
					// reached the final token of the prep phrase
					pp.setPhrase(buildPrepPhrase(words, i));
						
					// check negation, beginning with position of prep phrase start
					pp.setNegated(checkNegation(words, pp.getPhrase().get(0).getPosition()));
					
					// check intra-pp tokens for negation
					for(PrepPhraseToken token : pp.getPhrase()) {
						if(words.get(token.getPosition()).isNegationSignal()) {
							pp.setNegated(true);
							break;
						}
					}
					
					metadata.addPrepMetadata(pp);
				}

				
				/* VERB phrases (not handled in VerbPhraseHelper) */
				if(word.isInfinitiveVerb()) {
					VerbPhraseMetadata vp = new VerbPhraseMetadata(Constants.VerbClass.INFINITIVE);
					vp.addVerb(new VerbPhraseToken(word.getToken(), i));
					
					// infinitive follows prep phrase
//					try {
//						if(i > 0 && words.get(i-1).isPrepPhraseObject())
//							vp.setInfFollowsPP(true);
//					} catch(IndexOutOfBoundsException e) { }
					
					metadata.addVerbMetadata(vp);
				}	
					
				if(word.isPrepositionalVerb()) {
					VerbPhraseMetadata vp = new VerbPhraseMetadata(Constants.VerbClass.PREPOSITIONAL);
					vp.addVerb(new VerbPhraseToken(word.getToken(), i));
					
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
					
					// check intra-np tokens for negation
					for(GenericToken token : np.getPhrase()) {
						if(words.get(token.getPosition()).isNegationSignal()) {
							np.setNegated(true);
							break;
						}
					}
					
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
			// query lexicon for noun phrase semantic type
			//String st = Constants.semanticTypes.get(phrase.getNounPhraseString().trim().toLowerCase());
			
			String st;// = jedis.get("st:"+phrase.getNounPhraseString().trim().toLowerCase());

			try(Jedis jedis = Constants.MyJedisPool.INSTANCE.getResource()) {
				//list = jedis.hmget("ct:"+key, "attr", "qualifier");
				st = jedis.get("st:"+phrase.getNounPhraseString().trim().toLowerCase());
			}
			
			phrase.setSemanticType(st);
		}
		
		for(int j=0; j < metadata.getPrepMetadata().size(); j++) {
			PrepPhraseMetadata phrase = metadata.getPrepMetadata().get(j);
			
			if(j > 0) {
				// if prep phrase is negated are all grammatically-linked to it also negated?
				PrepPhraseMetadata prevPhrase = metadata.getPrepMetadata().get(j-1);
				
				if(prevPhrase.isNegated()) {
					int prevPhraseEndIdx = prevPhrase.getPhrase().get(prevPhrase.getPhrase().size()-1).getPosition();
					if(phrase.getPhrase().get(0).getPosition() - prevPhraseEndIdx == 1) {
						phrase.setNegated(true);
					}
				}
			}
			
			for(PrepPhraseToken ppt : phrase.getPhrase()) {
				ppt.setNounPhraseIdx(getContainingNounPhraseIdx(ppt.getPosition(), metadata.getNounMetadata()));
			}
		}
		
		for(VerbPhraseMetadata phrase : metadata.getVerbMetadata()) {
			if(phrase.getVerbClass() == Constants.VerbClass.ACTION ||
				phrase.getVerbClass() == Constants.VerbClass.LINKING_VERB ||
				phrase.getVerbClass() == Constants.VerbClass.VERB_OF_BEING || 
				phrase.getVerbClass() == Constants.VerbClass.MODAL_AUX) {
				
				for(VerbPhraseToken verb : phrase.getVerbs()) {
					verb.setNegated(checkNegation(words, verb.getPosition()));
					verb.setPrepPhrasesIdx(getModifyingPrepPhrases(verb.getPosition(), metadata.getPrepMetadata()));
				}
				
				// if verb consists of more than one token, query lexicon for semantic type
				if(phrase.getVerbs().size() > 1) {
					//phrase.setSemanticType(Constants.semanticTypes.get(phrase.getVerbString()));
					try(Jedis jedis = Constants.MyJedisPool.INSTANCE.getResource()) {
						phrase.setSemanticType(jedis.get("st:"+phrase.getVerbString().toLowerCase()));
					}
				}
				
				if(phrase.getSubj() != null) {
					phrase.getSubj().setPrepPhrasesIdx(getModifyingPrepPhrases(phrase.getSubj().getPosition(), metadata.getPrepMetadata()));
					phrase.getSubj().setNounPhraseIdx(getContainingNounPhraseIdx(phrase.getSubj().getPosition(), metadata.getNounMetadata()));
					phrase.getSubj().setNegated(checkSubjNegation(words, phrase.getSubj().getPosition()));
				}
				
				if(!phrase.getSubjC().isEmpty()) {
					for(VerbPhraseToken token : phrase.getSubjC()) {
						token.setPrepPhrasesIdx(getModifyingPrepPhrases(token.getPosition(), metadata.getPrepMetadata()));
						token.setNounPhraseIdx(getContainingNounPhraseIdx(token.getPosition(), metadata.getNounMetadata()));
						token.setNegated(checkSubjCNegation(words, token.getPosition()));
						
						token.setDepPhraseIdx(findModifyingDependentPhrase(words, token.getPosition(), metadata.getDependentMetadata()));
					}
				}
								
			} else if(phrase.getVerbClass() == Constants.VerbClass.PREPOSITIONAL) {
				//phrase.getVerb().setPrepPhrasesIdx(getModifyingPrepPhrases(phrase.getVerb().getPosition(), metadata.getPrepMetadata()));
				for(VerbPhraseToken verb : phrase.getVerbs()) {
					verb.setPrepPhrasesIdx(getModifyingPrepPhrases(verb.getPosition(), metadata.getPrepMetadata()));
				}
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
	
	private boolean checkNegation(ArrayList<WordToken> words, int tokenPos) {
		boolean ret = false;
		
		try {	
			// loop backward from tokenPos
			for(int i=tokenPos-1; i >= 0; i--) {
				WordToken word = words.get(i);

				if(words.get(i).isNegationSignal()) { // flipped this logic to allow "fail" to be detected as negation signal
					ret = true;
					break;
				} else if(word.isVerb() || word.isPrepPhraseObject()) { // break on a verb or prep phrase 
					break;
				}
			}
		} catch(IndexOutOfBoundsException oob) { }
		
		return ret;
	}
	
	private boolean checkSubjCNegation(ArrayList<WordToken> words, int tokenPos) {
		boolean ret = false;
		
		try {
			// loop backward from subject complement to verb
			for(int i=tokenPos-1; i >= 0; i--) {
				if(words.get(i).isNegationSignal()) {
					ret = true;
					break;
				} else if(words.get(i).isVerbOfBeing() || words.get(i).isLinkingVerb()) {
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
				if(words.get(i).isNegationSignal()) {
					ret = true;
					break;
				}
			}
		} catch(IndexOutOfBoundsException oob) { }
		
		return ret;
	}
	
	private boolean containsPattern(Pattern pattern, String fullSentence) {
		Matcher matcher = pattern.matcher(fullSentence);
		return matcher.find();
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
	
	private List<PrepPhraseToken> buildPrepPhrase(ArrayList<WordToken> words, int startPos) {
		List<PrepPhraseToken> list = new ArrayList<PrepPhraseToken>();
		
		for(int i=startPos; i >= 0; i--) {
			list.add(new PrepPhraseToken(words.get(i).getToken(), i, words.get(i).isPrepPhraseObject()));
			
			if(words.get(i).isPrepPhraseBegin())
				break;
		}
		
		if(list.isEmpty())
			return null;
		else
			return Lists.reverse(list);
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