package com.mst.interfaces;

import com.mst.model.gentwo.TokenRelationship;
import com.mst.model.gentwo.WordToken;

public interface TokenRelationshipFactory {

	TokenRelationship create(String edgeName,String frameName,WordToken fromToken,WordToken toToken);
}
