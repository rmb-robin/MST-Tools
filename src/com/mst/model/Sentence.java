package com.mst.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;

import com.google.common.base.Joiner;

public class Sentence {

	private int position;
	private long lineId;
	private String origSentence;
	private String normalizedSentence;
	private Date processDate;
	private Date procedureDate;
	private ArrayList<WordToken> wordList;
	private ArrayList<WordToken> nonPuncWordList = new ArrayList<>();
	private ArrayList<WordToken> puncOnlyWordList = new ArrayList<>();
	//private ArrayList<StanfordDependency> stanfordDependencyList;
	private SentenceMetadata metadata = new SentenceMetadata();
	private String id, source, practice, study;
	private HashMap<String, String> discrete = new HashMap<>();
	private List<ObjectId> structuredOIDs = new ArrayList<>();
	
	public Sentence() {	
		this.processDate = new Date();
	}
	
	public Sentence(String id, int position, ArrayList<WordToken> wordList) {	
		this.id = id;
		this.position = position;
		this.wordList = wordList;
		this.processDate = new Date();
	}

	public Sentence(String fullSentence) {	
		this.origSentence = fullSentence;
		this.normalizedSentence = fullSentence;
		this.processDate = new Date();
	}

	public List<GrammaticalPattern> getSentenceStructure() {
		
		List<GrammaticalPattern> patterns = new ArrayList<>();
		List<GrammaticalPatternEntity> entities = new ArrayList<>();
		
		Set<Integer> npIndexes = new HashSet<>();
		Set<Integer> ppIndexes = new HashSet<>();
		
		for(VerbPhraseMetadata vp : metadata.getVerbMetadata()) {
			GrammaticalPattern pattern = new GrammaticalPattern();
			
			// SUBJ
			if(vp.getSubj() != null) {
				// check for orphans between beginning of sentence and subj
				// starting at -1 will cause problems for multi-verb phrase sentences
				checkForOrphans(-1, vp.getSubj().getPosition(), pattern.getOtherOrphans());
				
				// subj and noun phrase which may contain it
				entities.add(new GrammaticalPatternEntity("vb-subj", 1));
				processNounPhraseGPE(vp.getSubj().getNounPhraseIdx(), npIndexes, false, entities);
				
				// prep phrases related to subj
				for(Integer idx : vp.getSubj().getPrepPhrasesIdx()) {
					processPrepPhraseGPE(idx, ppIndexes, npIndexes, entities);
				}
				
				// determine if orphans exist between the subj/vb
				int vbIdx = vp.getVerbs().get(0).getPosition();
				checkForOrphans(vp.getSubj().getPosition(), vbIdx, pattern.getVerbPhraseOrphans());
			}
			
			// VB
			int lastVbIdx = vp.getVerbs().get(vp.getVerbs().size()-1).getPosition();
			
			// determine if orphans exist between vb/vb
			checkForOrphans(vp.getVerbs().get(0).getPosition(), lastVbIdx, pattern.getVerbPhraseOrphans());
			
			// don't process PPs modifying the verb if the verb also happens to be the SUBJC, ex. "They were obtained before the..."
			// these PPs will get processed later
			if(!wordList.get(lastVbIdx).isSubjectComplement()) {
				entities.add(new GrammaticalPatternEntity("vb", vp.getVerbs().size()));
				
				for(VerbPhraseToken vpt : vp.getVerbs()) {
					for(Integer idx : vpt.getPrepPhrasesIdx()) {
						processPrepPhraseGPE(idx, ppIndexes, npIndexes, entities);
					}
				}
			} else {
				// avoid double-counting the final verb as a member of the verb phrase and also the SUBJC
				entities.add(new GrammaticalPatternEntity("vb", vp.getVerbs().size()-1));
			}
			
			// SUBJC
			for(VerbPhraseToken vpt : vp.getSubjC()) {
				entities.add(new GrammaticalPatternEntity("vb-obj", 1));
				processNounPhraseGPE(vpt.getNounPhraseIdx(), npIndexes, false, entities);
				
				for(Integer idx : vpt.getPrepPhrasesIdx()) {				
					processPrepPhraseGPE(idx, ppIndexes, npIndexes, entities);
				}
				
				// determine if orphans exist between vb/subjc and subjc/subjc
				checkForOrphans(lastVbIdx, vpt.getPosition(), pattern.getVerbPhraseOrphans());
				
				lastVbIdx = vpt.getPosition();
			}
			
			if(!vp.getSubjC().isEmpty()) {
				// check for orphans between beginning of sentence and subj
				// this will no doubt cause dupe entries with multi-verb phrases
				checkForOrphans(vp.getSubjC().get(vp.getSubjC().size()-1).getPosition(), wordList.size()-1, pattern.getOtherOrphans());
			}
			
			pattern.getEntities().addAll(entities);
			patterns.add(pattern);
			entities.clear();
		}
		
		// process noun phrases
		for(int i=0; i < metadata.getNounMetadata().size(); i++) {
			if(!npIndexes.contains(i) && !metadata.getNounMetadata().get(i).isWithinPP()) {
				processNounPhraseGPE(i, npIndexes, true, entities);
				
				for(Integer idx : metadata.getNounMetadata().get(i).getPrepPhrasesIdx()) {
					processPrepPhraseGPE(idx, ppIndexes, npIndexes, entities);
				}
				
				patterns.add(new GrammaticalPattern(entities));
				entities.clear();
			}
		}
		
		// process prep phrases
		for(int i=0; i < metadata.getPrepMetadata().size(); i++) {
			if(!ppIndexes.contains(i)) {
				processPrepPhraseGPE(i, ppIndexes, npIndexes, entities);
				patterns.add(new GrammaticalPattern(entities));
				entities.clear();
			}
		}
		
		//System.out.println(ppIndexes);
		//System.out.println(npIndexes);
		
		/*
		for(DependentPhraseMetadata dp : dependentPhrases) {
			int idx = dp.getPhrase().get(0).getPosition();
			markup.set(idx, dpOpen + markup.get(idx));
			
			idx = dp.getPhrase().get(dp.getPhrase().size()-1).getPosition();
			markup.set(idx, markup.get(idx) + dpClose);
		}
		*/
		
		return patterns;
	}
	
	private void checkForOrphans(int start, int end, List<WordToken> orphans) {
		for(int i=start+1; i < end; i++) {
			WordToken word = wordList.get(i);
			
			if(!ignoreToken(word)) {
				if(!word.isWithinNounPhrase() && 
				   !word.isWithinPrepPhrase() &&
				   !word.isVerb() &&
				   !word.isSubjectComplement() &&
				   !word.isVerbPhraseSubject()) {
					orphans.add(word);
				}
			}
		}
	}
	
	private String processNounPhrase(int idx, Set<Integer> processedIndexes, boolean npAlone) {
		String s = "";
		if(idx > -1) {
			s = npAlone ? "np" : "-np";
			processedIndexes.add(idx);
		}
		return s;
	}
	
	private GrammaticalPatternEntity processNounPhraseGPE(int idx, Set<Integer> processedIndexes, boolean npAlone, List<GrammaticalPatternEntity> entities) {
		GrammaticalPatternEntity e = null;
		if(idx > -1) {
			int npSize = getEntityTokenCount(metadata.getNounMetadata().get(idx).getPhrase().get(0).getPosition(), metadata.getNounMetadata().get(idx).getPhrase().size());
			if(npAlone) {
				e = new GrammaticalPatternEntity("np", npSize);
				entities.add(e);
			} else {
				e = entities.get(entities.size()-1); // get the last entity in the list
				e.setEntity(e.getEntity() + "-np"); // append the np notation
				if(npSize > e.getTokenCount())
					e.setTokenCount(npSize); // update the size to that of the np (if larger)
			}
			
			processedIndexes.add(idx);
		}
		return e;
	}
	
	private String processPrepPhrase(int idx, Set<Integer> ppIndexes, Set<Integer> npIndexes) {
		String s = "pp";
		
		PrepPhraseMetadata ppm = metadata.getPrepMetadata().get(idx);
		ppIndexes.add(idx);
		
		// loop backwards through each token of the PP, checking if it's a member of a NP
		for(int i=ppm.getPhrase().size()-1; i >= 0; i--) {
			String temp = processNounPhrase(ppm.getPhrase().get(i).getNounPhraseIdx(), npIndexes, false);
			if(temp.length() > 0) {
				s += temp;
				break;
			}
		}
		return s;
	}
	
	private GrammaticalPatternEntity processPrepPhraseGPE(int idx, Set<Integer> ppIndexes, Set<Integer> npIndexes, List<GrammaticalPatternEntity> entities) {
		GrammaticalPatternEntity e = null;
		
		List<PrepPhraseToken> pp = metadata.getPrepMetadata().get(idx).getPhrase();
		ppIndexes.add(idx);

		entities.add(new GrammaticalPatternEntity("pp", getEntityTokenCount(pp.get(0).getPosition(), pp.size())));
		
		// loop backwards through each token of the PP, checking if it's a member of a NP
		for(int i=pp.size()-1; i >= 0; i--) {
			int npIdx = pp.get(i).getNounPhraseIdx();
			if(npIdx > -1 && !npIndexes.contains(npIdx)) // PP token is part of a NP and we haven't already processed that NP
				processNounPhraseGPE(pp.get(i).getNounPhraseIdx(), npIndexes, false, entities);
			//if(processNounPhraseGPE(pp.get(i).getNounPhraseIdx(), npIndexes, false, entities) != null) {
			//	break;
			//}
		}

		return e;
	}
	
	private int getEntityTokenCount(int start, int end) {
		int count = 0;
		
		// certain types of tokens do not get considered when tallying the token count
		
		for(int i=start; i < start+end; i++) {
			WordToken word = wordList.get(i); 
			if(!ignoreToken(word))
				count++;
		}
		
		return count;
	}
	
	public int getCuratedTokenCount() {
		return getEntityTokenCount(0, wordList.size());
	}
	
	private boolean ignoreToken(WordToken word) {
		return word.isPunctuation() || 
			   word.isDeterminerPOS() || 
			   word.isConjunctionPOS() || 
			   word.isNegationSignal();
	}
	
	public SentenceMetadata getMetadata() { return metadata; }
	
	public void setMetadata(SentenceMetadata val) { metadata = val; }
	
	public HashMap<String, String> getDiscrete() {
		return this.discrete;
	}
	
	public void setDiscrete(HashMap<String, String> map) {
		this.discrete = map;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	public void setLineId(long lineId) {
		this.lineId = lineId;
	}
	
	public long getLineId() {
		return lineId;
	}
	
	public void setPractice(String practice) {
		this.practice = practice;
	}
	
	public String getPractice() {
		return practice;
	}
	
	public void setStudy(String study) {
		this.study = study;
	}
	
	public String getStudy() {
		return study;
	}
	
	public void setSource(String source) {
		this.source = source;
	}
	
	public String getSource() {
		return source;
	}
	
	public int getPosition() {
		return position;
	}
	
	public void setPosition(int position) {
		this.position = position;
	}
	
	public String getFullSentence() {
		return normalizedSentence;
	}
	
	public void setFullSentence(String sentence) {
		this.normalizedSentence = sentence;
	}
	
	public String getOrigSentence() {
		return origSentence;
	}
	
	public void setOrigSentence(String sentence) {
		this.origSentence = sentence;
	}
	
	public void setWordList(ArrayList<WordToken> wordList) {
		this.wordList = wordList;
	}
	
	public ArrayList<WordToken> getWordList() {
		return this.wordList;
	}
	
	public void setNonPuncWordList(ArrayList<WordToken> nonPuncWordList) {
		this.nonPuncWordList = nonPuncWordList;
	}
	
	public ArrayList<WordToken> getNonPuncWordList() {
		return this.nonPuncWordList;
	}
	
	public void setPuncOnlyWordList(ArrayList<WordToken> puncOnlyWordList) {
		this.puncOnlyWordList = puncOnlyWordList;
	}
	
	public ArrayList<WordToken> getPuncOnlyWordList() {
		return this.puncOnlyWordList;
	}
	
	public void setProcessDate(Date processDate) {
		this.processDate = processDate;
	}
	
	public Date getProcessDate() {
		return this.processDate;
	}

	public void setProcedureDate(Date procedureDate) {
		this.procedureDate = procedureDate;
	}
	
	public Date getProcedureDate() {
		return this.procedureDate;
	}
	
	public List<ObjectId> getStructuredOIDs() {
		return structuredOIDs;
	}
	
	public void setStructuredOIDs(List<ObjectId> structuredOIDs) {
		this.structuredOIDs = structuredOIDs;
	}
}
