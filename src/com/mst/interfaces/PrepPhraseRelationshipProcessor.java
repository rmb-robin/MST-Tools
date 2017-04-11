package com.mst.interfaces;

import java.util.List;

import com.mst.model.WordToken;
import com.mst.model.gentwo.PrepPhraseRelationshipMapping;
import com.mst.model.gentwo.TokenRelationship;

public interface PrepPhraseRelationshipProcessor {

	List<TokenRelationship> process(List<WordToken> tokens, List<PrepPhraseRelationshipMapping> prepPhraseRelationshipMappings);
}