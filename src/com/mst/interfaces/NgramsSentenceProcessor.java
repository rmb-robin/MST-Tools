package com.mst.interfaces;

import java.util.List;

import com.mst.model.NGramsModifierEntity;
import com.mst.model.Sentence;

public interface NgramsSentenceProcessor {

	Sentence process(Sentence sentence, List<NGramsModifierEntity> ngramsModifierEntities);
}
