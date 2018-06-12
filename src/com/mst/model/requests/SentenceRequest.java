package com.mst.model.requests;

import java.util.ArrayList;
import java.util.List;

public class SentenceRequest extends SentenceRequestBase {
    private List<String> sentenceTexts;

    public SentenceRequest() {
        sentenceTexts = new ArrayList<>();
    }

    public List<String> getSentenceTexts() {
        return sentenceTexts;
    }

    public void setSentenceTexts(List<String> sentenceTexts) {
        this.sentenceTexts = sentenceTexts;
    }
}
