package com.mst.model.SentenceQuery;

import java.util.ArrayList;
import java.util.List;

import com.mst.model.sentenceProcessing.SentenceDb;

public class SentenceQueryInstanceResult {
    private List<SentenceQueryResult> sentenceQueryResult;
    private List<SentenceDb> sentences;

    public SentenceQueryInstanceResult() {
        sentenceQueryResult = new ArrayList<>();
        sentences = new ArrayList<>();
    }

    public List<SentenceQueryResult> getSentenceQueryResult() {
        return sentenceQueryResult;
    }

    public void setSentenceQueryResult(List<SentenceQueryResult> sentenceQueryResult) {
        this.sentenceQueryResult = sentenceQueryResult;
    }

    public List<SentenceDb> getSentences() {
        return sentences;
    }

    public void setSentences(List<SentenceDb> sentences) {
        this.sentences = sentences;
    }
}
