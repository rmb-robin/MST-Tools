package com.mst.interfaces;

import java.util.List;

import com.mst.model.WordToken;
import com.mst.model.gentwo.PartOfSpeechAnnotatorEntity;

public interface PartOfSpeechAnnotator {

	List<WordToken> annotate(List<WordToken> tokens,PartOfSpeechAnnotatorEntity entity);
}
