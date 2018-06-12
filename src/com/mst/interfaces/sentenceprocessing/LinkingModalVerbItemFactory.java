package com.mst.interfaces.sentenceprocessing;

import com.mst.model.sentenceProcessing.LinkingModalVerbItem;

public interface LinkingModalVerbItemFactory {

	LinkingModalVerbItem create(String verbType, String verbTense, String token, String state);
}
