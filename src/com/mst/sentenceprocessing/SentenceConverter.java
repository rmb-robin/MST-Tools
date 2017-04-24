package com.mst.sentenceprocessing;

import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceDb;

public class SentenceConverter {

	public static SentenceDb convertToDocument(Sentence sentence){
		SentenceDb dbObj = new SentenceDb();
		dbObj.setModifiedWordList(sentence.getModifiedWordList());
		dbObj.setOriginalWords(sentence.getOriginalWords());
		dbObj.setOrigSentence(sentence.getOrigSentence());
		dbObj.setTokenRelationships(sentence.getTokenRelationships());
		dbObj.setPractice(sentence.getPractice());
		//dbObj.setProcessDate(sentence.getProcessDate());
		dbObj.setSource(sentence.getSource());
		dbObj.setStudy(sentence.getStudy());
		dbObj.setNormalizedSentence(sentence.getNormalizedSentence());
		return dbObj;
	}
}
