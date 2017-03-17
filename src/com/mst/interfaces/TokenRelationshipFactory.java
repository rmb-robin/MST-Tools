package com.mst.interfaces;

import com.mst.model.WordToken;
import com.mst.model.gentwo.TokenRelationship;

public interface TokenRelationshipFactory {

	TokenRelationship create(String edgeName,String frameName,WordToken fromToken,WordToken toToken);
}
