package com.mst.interfaces;

import java.util.List;

import com.mst.model.gentwo.PartOfSpeechAnnotatorEntity;
import com.mst.model.gentwo.WordToken;

public interface PartOfSpeechAnnotator {

	List<WordToken> annotate(List<WordToken> tokens,PartOfSpeechAnnotatorEntity entity);
}
