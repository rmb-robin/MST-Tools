package test;

import com.mst.dao.BaseDocumentDaoImpl;
import com.mst.model.SentenceQuery.SentenceQueryInput;
import org.mongodb.morphia.query.Query;

import java.util.List;

public class SentenceQueryInputDao extends BaseDocumentDaoImpl<SentenceQueryInput> {
    public SentenceQueryInputDao(Class<SentenceQueryInput> entityClass) {
        super(entityClass);
    }

    void saveInput(SentenceQueryInput input) {
        super.save(input);
    }

    public List<SentenceQueryInput> getInput(String orgId) {
        Query<SentenceQueryInput> query = this.getDatastore().createQuery(SentenceQueryInput.class);
        query.field("organizationId").equal(orgId);
        return query.asList();
    }
}
