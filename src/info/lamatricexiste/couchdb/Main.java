package info.lamatricexiste.couchdb;

import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.couchbase.libcouch.ICouchClient;
import com.couchbase.libcouch.ICouchService;

public class Main extends Activity {

    private final static String ACTION = "com.couchone.libcouch.ICouchService";
    ICouchService couchService;

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
        bindService(new Intent(ACTION), couchServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection couchServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            couchService = ICouchService.Stub.asInterface(service);
            try {
                couchService.initCouchDB(couchClient, null, "release-0.1");
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            couchService = null;
        }

    };

    private ICouchClient couchClient = new ICouchClient.Stub() {

        @Override
        public void couchStarted(String host, int port) throws RemoteException {
            Log.e("Main", "host=" + host + ", port=" + port);
        }

        @Override
        public void exit(String error) throws RemoteException {
        }

        @Override
        public void installing(int completed, int total) throws RemoteException {
            Log.e("INSTALL", completed + " / " + total);
        }

    };

    private void sendRequest(String host, int port, JSONObject req) {
        try {
            URI requestUri = new URI("http", "", host, port, "Contacts", "", "");
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
