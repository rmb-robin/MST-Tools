package com.mst.interfaces;

import java.util.List;

import com.mst.model.WordToken;

public interface PrepositionPhraseProcessor {

	List<WordToken> process(List<WordToken> tokens);
}
