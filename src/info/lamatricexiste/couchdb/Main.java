package info.lamatricexiste.couchdb;

import android.app.Activity;
import android.os.Bundle;

public class Main extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        String release = "release-0.1";
        ServiceConnection couchServiceConnection = CouchDB.getService(getBaseContext(), null, release, mCallback);
    }

    private final ICouchClient mCallback = new ICouchClient.Stub() {
        @Override
        public void couchStarted(String host, int port) {}

        @Override
        public void installing(int completed, int total) {}

        @Override
        public void downloading(int completed, int total) {}

        @Override
        public void exit(String error) {}
    };
}
