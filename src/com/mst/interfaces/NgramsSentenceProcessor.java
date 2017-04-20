package com.mst.interfaces;

import java.util.List;

import com.mst.model.sentenceProcessing.NGramsModifierEntity;
import com.mst.model.sentenceProcessing.Sentence;

public interface NgramsSentenceProcessor {

	Sentence process(Sentence sentence, List<NGramsModifierEntity> ngramsModifierEntities);
}
