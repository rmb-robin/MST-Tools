package com.mst.interfaces.sentenceprocessing;

import java.util.List;

import com.mst.model.sentenceProcessing.NGramsModifierEntity;

public interface NGramsProvider {

	List<NGramsModifierEntity> getNGrams();
}
