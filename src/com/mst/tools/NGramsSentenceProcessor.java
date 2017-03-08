package com.mst.tools;

import com.mst.interfaces.SentenceProcesser;
import com.mst.model.Sentence;

public class NGramsSentenceProcessor implements  SentenceProcesser {

	public Sentence process(Sentence sentence) {
		return sentence;
	}
}
