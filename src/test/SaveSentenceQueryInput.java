package test;

import com.mst.model.SentenceQuery.SentenceQueryInput;
import com.mst.util.MongoDatastoreProviderDefault;

public class SaveSentenceQueryInput {
    private final boolean ON = false;
    private SentenceQueryInputDao dao;

    public SaveSentenceQueryInput() {
        String SERVER = "10.0.129.218";
        String DATABASE = "test";
        dao = new SentenceQueryInputDao(SentenceQueryInput.class);
        dao.setMongoDatastoreProvider(new MongoDatastoreProviderDefault(SERVER, DATABASE));
    }

    public void process(SentenceQueryInput input) {
        if (ON) {
            input.setDebug(true);
            dao.saveInput(input);
        }
    }
}
