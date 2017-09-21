package com.mst.dao;

import java.util.HashSet;
import java.util.List;

import org.mongodb.morphia.query.Query;

import com.mst.interfaces.dao.RecommendedTokenRelationshipDao;
import com.mst.model.sentenceProcessing.RecommandedTokenRelationship;

public class RecommendedTokenRelationshipDaoImpl extends BaseDocumentDaoImpl<RecommandedTokenRelationship> implements RecommendedTokenRelationshipDao {

	public RecommendedTokenRelationshipDaoImpl() {
		super(RecommandedTokenRelationship.class);
	}

	@Override
	public List<RecommandedTokenRelationship> queryByKey(HashSet<String> keys) {
		Query<RecommandedTokenRelationship> query = datastoreProvider.getDataStore().createQuery(RecommandedTokenRelationship.class);
		 query
		 .field("key").hasAnyOf(keys);
		 return query.asList();
	}
}
