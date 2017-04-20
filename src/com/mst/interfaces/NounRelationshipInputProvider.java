package com.mst.interfaces;

import com.mst.model.sentenceProcessing.RelationshipInput;

public interface NounRelationshipInputProvider {

	RelationshipInput getNounRelationships(int maxDistance);
}
