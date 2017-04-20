package com.mst.interfaces;

import java.util.List;

import com.mst.model.gentwo.PrepPhraseRelationshipMapping;
import com.mst.model.gentwo.TokenRelationship;
import com.mst.model.gentwo.WordToken;

public interface PrepPhraseRelationshipProcessor {

	List<TokenRelationship> process(List<WordToken> tokens, List<PrepPhraseRelationshipMapping> prepPhraseRelationshipMappings);
}