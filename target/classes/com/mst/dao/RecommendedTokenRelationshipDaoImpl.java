package com.mst.dao;

import java.util.HashSet;
import java.util.List;

import org.mongodb.morphia.query.Query;

import com.mst.interfaces.dao.RecommendedTokenRelationshipDao;
import com.mst.model.recommandation.RecommendedTokenRelationship;

public class RecommendedTokenRelationshipDaoImpl extends BaseDocumentDaoImpl<RecommendedTokenRelationship> implements RecommendedTokenRelationshipDao {

	public RecommendedTokenRelationshipDaoImpl() {
		super(RecommendedTokenRelationship.class);
	}

	@Override
	public List<RecommendedTokenRelationship> queryByKey(HashSet<String> keys) {
		Query<RecommendedTokenRelationship> query = datastoreProvider.getDefaultDb().createQuery(RecommendedTokenRelationship.class);
		 query
		 .field("key").hasAnyOf(keys);
		 return query.asList();
	}

	@Override
	public List<RecommendedTokenRelationship> getVerified() {
		Query<RecommendedTokenRelationship> query = datastoreProvider.getDefaultDb().createQuery(RecommendedTokenRelationship.class);
		 query
		 .field("isVerified").equal(true);
		 return query.asList();
	}
}
