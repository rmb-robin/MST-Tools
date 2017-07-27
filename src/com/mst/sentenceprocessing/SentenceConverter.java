package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.List;

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
		dbObj.setProcessingDate(sentence.getProcessDate());
		dbObj.setSource(sentence.getSource());
		dbObj.setStudy(sentence.getStudy());
		dbObj.setNormalizedSentence(sentence.getNormalizedSentence());
		dbObj.setDiscreteData(sentence.getDiscreteData());
		dbObj.setOrganizationId(sentence.getOrganizationId());
		dbObj.setDidFail(sentence.isDidFail());
		return dbObj;
	}
	
	public static List<Sentence> convertToSentence(List<SentenceDb> sentencedbobjects){
		List<Sentence> sentences = new ArrayList<>();
		
			for(SentenceDb sentencedb: sentencedbobjects){
				Sentence sentence = new Sentence();
				sentence.setModifiedWordList(sentencedb.getModifiedWordList());
				sentence.setOriginalWords(sentencedb.getOriginalWords());
				sentence.setOrigSentence(sentencedb.getOrigSentence());
				sentence.setTokenRelationships(sentencedb.getTokenRelationships());
				sentence.setPractice(sentencedb.getPractice());
			//	sentence.setProcessDate(sentencedb.getProcessingDate());
				sentence.setSource(sentencedb.getSource());
				sentence.setStudy(sentencedb.getStudy());
				sentence.setNormalizedSentence(sentencedb.getNormalizedSentence());
				sentence.setDiscreteData(sentencedb.getDiscreteData());
				sentence.setOrganizationId(sentencedb.getOrganizationId());
				sentence.setDidFail(sentencedb.isDidFail());
				sentences.add(sentence);
			}
			return sentences;
	}
	
}
