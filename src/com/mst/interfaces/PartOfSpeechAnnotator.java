package com.mst.interfaces;

import java.util.List;


import com.mst.model.PartOfSpeechAnnotatorEntity;
import com.mst.model.WordToken;

public interface PartOfSpeechAnnotator {

	List<WordToken> annotate(List<WordToken> tokens,PartOfSpeechAnnotatorEntity entity);
}
