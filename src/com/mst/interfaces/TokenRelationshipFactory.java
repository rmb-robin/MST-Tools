package com.mst.interfaces;

import com.mst.model.sentenceProcessing.TokenRelationship;
import com.mst.model.sentenceProcessing.WordToken;

public interface TokenRelationshipFactory {

	TokenRelationship create(String edgeName,String frameName,WordToken fromToken,WordToken toToken);
}
