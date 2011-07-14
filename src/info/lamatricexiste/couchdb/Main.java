package info.lamatricexiste.couchdb;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.couchbase.libcouch.ICouchClient;
import com.couchbase.libcouch.ICouchService;

public class Main extends Activity {

    private final static String ACTION = "com.couchone.libcouch.ICouchService";
    private String mHost;
    private int mPort;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);
        startCouch();

        // Buttons
        findViewById(R.id.btn_rep1).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Server -> Local
                new RequestTask(mHost, mPort, "POST", "_replicate", "{\"source\":\""
                        + getString(R.string.server_master)
                        + "\",\"target\":\"contacts\",\"create_target\":true,\"continuous\":true}")
                        .execute();
            }
        });
        findViewById(R.id.btn_rep2).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Local -> Server
                new RequestTask(mHost, mPort, "POST", "_replicate",
                        "{\"source\":\"contacts\",\"target\":\""
                                + getString(R.string.server_master) + "\"}").execute();
            }
        });
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
        private ICouchService couchService;

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
            mHost = host;
            mPort = port;
        }

        @Override
        public void exit(String error) throws RemoteException {
            Log.e("EXIT", error);
        }

        @Override
        public void installing(int completed, int total) throws RemoteException {
            Log.e("INSTALL", completed + " / " + total);
        }

    };

    private class RequestTask extends AsyncTask<Void, Void, String> {

        private URL url;
        private String method;
        private String data;

        RequestTask(String host, int port, String method, String action, String data) {
            this.method = method;
            this.data = data;
            try {
                url = new URL("http://" + host + ":" + port + "/" + action);
            }
            catch (MalformedURLException e) {}
        }

        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected String doInBackground(Void... params) {
            StringBuffer sb = new StringBuffer();
            try {
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
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
                    BufferedReader rd = new BufferedReader(
                            new InputStreamReader(c.getInputStream()));
                    String line;
                    while ((line = rd.readLine()) != null) {
                        // Debug
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
            return sb.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            setProgressBarIndeterminateVisibility(false);
            ((TextView) findViewById(R.id.output)).setText(result);
        }

        @Override
        protected void onCancelled() {
            setProgressBarIndeterminateVisibility(false);
            ((TextView) findViewById(R.id.output)).setText("Cancelled");
        }

    }
}
