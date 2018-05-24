package com.mst.autocomplete.interfaces;

import java.util.List;

import com.mst.model.autocomplete.AutoCompleteRequest;

public interface RecommendedTokenRelationshipAutocompleteQuery {

	List<String> getNextWord(AutoCompleteRequest request);
	
}
