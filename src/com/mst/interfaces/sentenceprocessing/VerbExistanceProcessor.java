package com.mst.interfaces.sentenceprocessing;

import java.util.List;

import com.mst.model.sentenceProcessing.Sentence;
import com.mst.model.sentenceProcessing.TokenRelationship;

public interface VerbExistanceProcessor {

	List<TokenRelationship> process(Sentence sentence);
}
