package info.lamatricexiste.couchdb;

import android.app.Activity;
import android.content.ServiceConnection;
import android.os.Bundle;

import com.couchbase.libcouch.CouchDB;
import com.couchbase.libcouch.ICouchClient;

public class Main extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        String release = "release-0.1";
        CouchDB.getService(getBaseContext(), null, release, mCallback);
    }

    private final ICouchClient mCallback = new ICouchClient.Stub() {
        @Override
        public void couchStarted(String host, int port) {
        }

        @Override
        public void installing(int completed, int total) {
        }

        // @Override
        // public void downloading(int completed, int total) {
        // }

        @Override
        public void exit(String error) {
        }
    };
}
