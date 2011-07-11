package info.lamatricexiste.couchdb;

import android.app.Activity;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.util.Log;

import com.couchbase.libcouch.CouchDB;
import com.couchbase.libcouch.ICouchClient;

public class Main extends Activity {

    private ServiceConnection couchServiceConnection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        startCouch();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        startCouch();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unbindService(couchServiceConnection);
        }
        catch (IllegalArgumentException e) {}
    }

    private void startCouch() {
        couchServiceConnection = CouchDB.getService(getBaseContext(), null, "release-0.1",
                mCallback);
    }

    private final ICouchClient mCallback = new ICouchClient.Stub() {
        @Override
        public void couchStarted(String host, int port) {
            Log.e("COUCH", "host=" + host + ", port=" + port);
        }

        @Override
        public void installing(int completed, int total) {
            Log.e("COUCH", "completed=" + completed + ", total=" + total);
        }

        @Override
        public void exit(String error) {
            Log.e("COUCH", "exit=" + error);
        }
    };
}
