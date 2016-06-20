package com.mst.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;
import com.mst.model.GrammaticalPattern;
import com.mst.model.Sentence;
import com.mst.model.SentenceMetadata;
import com.mst.model.WordToken;

public class GrammaticalPatternHelper {
	private String pattern = "";
	private String status = "";
	private int count = -1;
	private String verbPhraseOrphans = "";
	private String otherOrphans = "";
	
	public GrammaticalPatternHelper(Sentence s) {
		generatePattern(s);
	}
	
	private void generatePattern(Sentence s) {
		List<GrammaticalPattern> patterns = s.getSentenceStructure(); 

		//int entityCount = 0; 
		int entityCount2 = 0;
		
		List<WordToken> allVBOrphans = new ArrayList<>();
		List<WordToken> allOtherOrphans = new ArrayList<>();
		List<String> allPatterns = new ArrayList<>();
		
		for(GrammaticalPattern pattern : patterns) {
			//entityCount += pattern.getPatternTokenCount();
			allVBOrphans.addAll(pattern.getVerbPhraseOrphans());
			allOtherOrphans.addAll(pattern.getOtherOrphans());
			allPatterns.add(pattern.toString());
		}
		
		for(WordToken word : s.getWordList())
			if(word.isWithinNounPhrase() || word.isWithinPrepPhrase() || word.isWithinVerbPhrase())
				if(!(word.isPunctuation() || word.isDeterminerPOS() || word.isConjunctionPOS() ||  word.isNegationSignal()))
					entityCount2++;
		
		// get entityCount from metadata. switched to this when modifiers were added to verbphrasetokens ("He is really tired.")
		SentenceMetadata m = s.getMetadata();
		Set<Integer> mIndexes = m.getIncludedIndexes();
		
//		System.out.println(s.getFullSentence() + "   entityCount2: " + entityCount2 + "   mIndexes: " + mIndexes.size());
		
		if(allPatterns.isEmpty()) {
			pattern = "frag";
		
			if(s.getFullSentence().matches("\\d\\.?"))
				status = "OK_FRAG-NUMERIC";
			else
				status = "OK_FRAG";
		} else {
			pattern = Joiner.on("; ").join(allPatterns);
			
			//if(entityCount2 == s.getCuratedTokenCount() && allVBOrphans.isEmpty() && allOtherOrphans.isEmpty()) {
			if(mIndexes.size() == s.getCuratedTokenCount() && allVBOrphans.isEmpty() && allOtherOrphans.isEmpty()) {
				// in certain cases the counts are equal but we still have orphans. example is when subj/subjc across two verb phrases are shared
				status = "OK";
			} else {
				verbPhraseOrphans = allVBOrphans.isEmpty() ? "" : allVBOrphans.toString().substring(1, allVBOrphans.toString().length()-1);
				otherOrphans = allOtherOrphans.isEmpty() ? "" : allOtherOrphans.toString().substring(1, allOtherOrphans.toString().length()-1);
				
				if(!allVBOrphans.isEmpty() && !allOtherOrphans.isEmpty())
					status = "ORPHAN_MULTI";
				else if(!allVBOrphans.isEmpty())
					status = "ORPHAN_IN_VB";
				else 
					status = "ORPHAN";
			}
		}
			
		count = allPatterns.size();
	}
	
	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getVerbPhraseOrphans() {
		return verbPhraseOrphans;
	}

	public void setVerbPhraseOrphans(String verbPhraseOrphans) {
		this.verbPhraseOrphans = verbPhraseOrphans;
	}

	public String getOtherOrphans() {
		return otherOrphans;
	}

	public void setOtherOrphans(String otherOrphans) {
		this.otherOrphans = otherOrphans;
	}
}
