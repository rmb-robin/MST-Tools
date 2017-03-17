package com.mst.interfaces;

import com.mst.model.gentwo.NounRelationshipInput;

public interface NounRelationshipInputProvider {

	NounRelationshipInput get(String frameName, int maxDistance);
}
