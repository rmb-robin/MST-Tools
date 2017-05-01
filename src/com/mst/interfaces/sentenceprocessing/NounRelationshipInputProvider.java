package com.mst.interfaces.sentenceprocessing;

import com.mst.model.sentenceProcessing.RelationshipInput;

public interface NounRelationshipInputProvider {

	RelationshipInput getNounRelationships(int maxDistance);
}
