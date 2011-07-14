package info.lamatricexiste.couchdb;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
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
            JSONObject json;
            try {
                // Replication test
                json = new JSONObject("{'source':'" + getString(R.string.server_master)
                        + "','target':'contacts','create_target':true}");
                sendRequest(host, port, "POST", "_replicate", json.toString());
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void exit(String error) throws RemoteException {
        }

        @Override
        public void installing(int completed, int total) throws RemoteException {
            Log.e("INSTALL", completed + " / " + total);
        }

    };

    private void sendRequest(String host, int port, String method, String action, String data) {
        StringBuffer sb = new StringBuffer();
        try {
            HttpURLConnection c = (HttpURLConnection) new URL("http://" + host + ":" + port + "/"
                    + action).openConnection();
            String charEncoding = "iso-8859-1";
            c.setDoOutput(true);
            c.setUseCaches(false);
            c.setRequestMethod(method);
            c.setRequestProperty("Content-type", "application/json; charset=UTF-8");

            if (method != "GET" && data != null) {
                c.setDoInput(true);
                c.setRequestProperty("Content-Length", Integer.toString(data.length()));
                c.getOutputStream().write(data.getBytes(charEncoding));
            }
            c.connect();
            try {
                BufferedReader rd = new BufferedReader(new InputStreamReader(c.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                rd.close();
            }
            catch (FileNotFoundException e) {}
            catch (NullPointerException e) {}
            finally {
                c.disconnect();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
