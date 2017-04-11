package com.mst.interfaces;

import com.mst.model.gentwo.RelationshipInput;

public interface NounRelationshipInputProvider {

	RelationshipInput getNounRelationships(String frameName, int maxDistance);
}
