package com.mst.model.test;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Entity("sentenceCompareSummary")
public class SentenceCompareSummary {

	@Id
	private ObjectId id;
   
	
	private int totalSentencesProcessed; 
	private int totalSentencesMatched; 
	private int totalSentenceMismatched; 
	
	private List<SingleSentenceCompare> mismatchedSentences;
	private List<SingleSentenceCompare> matchedSentences;

	public SentenceCompareSummary(){
		mismatchedSentences = new ArrayList<>();
	}
	
	
	public int getTotalSentencesProcessed() {
		return totalSentencesProcessed;
	}

	public void setTotalSentencesProcessed(int totalSentencesProcessed) {
		this.totalSentencesProcessed = totalSentencesProcessed;
	}

	public int getTotalSentencesMatched() {
		return totalSentencesMatched;
	}

	public void setTotalSentencesMatched(int totalSentencesMatched) {
		this.totalSentencesMatched = totalSentencesMatched;
	}

	public int getTotalSentenceMismatched() {
		return totalSentenceMismatched;
	}

	public void setTotalSentenceMismatched(int totalSentenceMismatched) {
		this.totalSentenceMismatched = totalSentenceMismatched;
	}

	public List<SingleSentenceCompare> getMismatchedSentences() {
		return mismatchedSentences;
	}

	public void setMismatchedSentences(List<SingleSentenceCompare> mismatchedSentences) {
		this.mismatchedSentences = mismatchedSentences;
	} 
	
}
