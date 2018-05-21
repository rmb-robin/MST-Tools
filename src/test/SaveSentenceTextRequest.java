package test;

import com.mst.model.requests.SentenceTextRequest;
import com.mst.util.MongoDatastoreProviderDefault;

public class SaveSentenceTextRequest {
    private final boolean ON = false;
    private SentenceTextRequestDao dao;

    public SaveSentenceTextRequest() {
        String SERVER = "10.0.129.218";
        String DATABASE = "test";
        dao = new SentenceTextRequestDao(SentenceTextRequest.class);
        dao.setMongoDatastoreProvider(new MongoDatastoreProviderDefault(SERVER, DATABASE));
    }

    public void process(SentenceTextRequest request) {
        if (ON)
            dao.saveRequest(request);
    }
}
