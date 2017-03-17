package com.mst.interfaces;

import java.util.List;

import com.mst.model.Sentence;
import com.mst.model.gentwo.NGramsModifierEntity;

public interface NgramsSentenceProcessor {

	Sentence process(Sentence sentence, List<NGramsModifierEntity> ngramsModifierEntities);
}
