package test;

import com.mst.dao.BaseDocumentDaoImpl;
import com.mst.model.requests.SentenceTextRequest;
import org.mongodb.morphia.query.Query;

import java.util.List;

public class SentenceTextRequestDao extends BaseDocumentDaoImpl<SentenceTextRequest> {
    public SentenceTextRequestDao(Class<SentenceTextRequest> entityClass) {
        super(entityClass);
    }

    void saveRequest(SentenceTextRequest request) {
        super.save(request);
    }

    public List<SentenceTextRequest> getRequest(String source) {
        Query<SentenceTextRequest> query = this.getDatastore().createQuery(SentenceTextRequest.class);
        query.field("source").equal(source);
        return query.asList();
    }
}
