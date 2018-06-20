package com.mst.sentenceprocessing;

import java.util.ArrayList;
import java.util.List;

import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.SentenceDb;
import org.bson.types.ObjectId;

public class SentenceConverter {

    public static SentenceDb convertToSentenceDb(Sentence sentence, boolean createObjectId) {
        SentenceDb sentenceDb = new SentenceDb();
        if (createObjectId)
            sentenceDb.setId(new ObjectId());
        sentenceDb.setModifiedWordList(sentence.getModifiedWordList());
        sentenceDb.setOriginalWords(sentence.getOriginalWords());
        sentenceDb.setOrigSentence(sentence.getOrigSentence());
        sentenceDb.setTokenRelationships(sentence.getTokenRelationships());
        sentenceDb.setPractice(sentence.getPractice());
        sentenceDb.setProcessingDate(sentence.getProcessDate());
        sentenceDb.setSource(sentence.getSource());
        sentenceDb.setStudy(sentence.getStudy());
        sentenceDb.setNormalizedSentence(sentence.getNormalizedSentence());
        sentenceDb.setDiscreteData(sentence.getDiscreteData());
        sentenceDb.setOrganizationId(sentence.getOrganizationId());
        sentenceDb.setDidFail(sentence.isDidFail());
        return sentenceDb;
    }

    public static List<Sentence> convertToSentence(List<SentenceDb> sentenceDbs, boolean addRelationships, boolean addModifiedList, boolean addFailure) {
        List<Sentence> sentences = new ArrayList<>();
        for (SentenceDb sentenceDb : sentenceDbs) {
            Sentence sentence = new Sentence();
            sentence.setId(sentenceDb.getId().toString());
            if (addModifiedList) {
                sentence.setModifiedWordList(sentenceDb.getModifiedWordList());
                sentence.setNormalizedSentence(sentenceDb.getNormalizedSentence());
            }
            sentence.setOriginalWords(sentenceDb.getOriginalWords());
            sentence.setOrigSentence(sentenceDb.getOrigSentence());
            if (addRelationships)
                sentence.setTokenRelationships(sentenceDb.getTokenRelationships());
            sentence.setPractice(sentenceDb.getPractice());
            //	sentence.setProcessDate(sentenceDb.getProcessingDate());
            sentence.setSource(sentenceDb.getSource());
            sentence.setStudy(sentenceDb.getStudy());
            sentence.setDiscreteData(sentenceDb.getDiscreteData());
            sentence.setOrganizationId(sentenceDb.getOrganizationId());
            if (addFailure)
                sentence.setDidFail(sentenceDb.isDidFail());
            sentences.add(sentence);
        }
        return sentences;
    }
}
