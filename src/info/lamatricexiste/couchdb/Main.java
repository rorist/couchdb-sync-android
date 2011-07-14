package info.lamatricexiste.couchdb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
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
                byte[] data = sendRequest(host, port, json.toString());
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

    private byte[] sendRequest(String host, int port, String req) {
        HttpEntity entity = null;
        InputStream is = null;
        HttpPost method = null;
        byte[] data = null;
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            method = new HttpPost(new URI("http", null, host, port, "/_replicate", null, null));
            method.addHeader("Content-type", "application/json; charset=UTF-8");
            method.setEntity(new StringEntity(req));
            HttpResponse response = client.execute(method);
            entity = response.getEntity();
            is = entity.getContent();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (is != null) {
                data = readFully(is);
            }
            if (method != null) {
                method.abort();
            }
            if (entity != null) {
                try {
                    entity.consumeContent();
                }
                catch (IOException e) {}
            }
            if (is != null) {
                try {
                    is.close();
                }
                catch (final IOException ignore) {}
            }
        }
        return data;
    }

    private byte[] readFully(final InputStream is) {
        ByteArrayOutputStream out = null;
        final byte[] buffer = new byte[1024];
        byte[] result;
        try {
            out = new ByteArrayOutputStream();
            int read;
            while ((read = is.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
            result = out.toByteArray();
        }
        catch (final IOException e) {
            result = new byte[0];
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (final IOException ignore) {}
            }
        }
        return result;
    }
}
