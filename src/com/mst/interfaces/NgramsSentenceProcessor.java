package com.mst.interfaces;

import java.util.List;

import com.mst.model.gentwo.NGramsModifierEntity;
import com.mst.model.gentwo.Sentence;

public interface NgramsSentenceProcessor {

	Sentence process(Sentence sentence, List<NGramsModifierEntity> ngramsModifierEntities);
}
