package info.lamatricexiste.budiez;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.ContactsContract.Data;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.couchbase.libcouch.ICouchClient;
import com.couchbase.libcouch.ICouchService;

public class Main extends Activity {

    // private static final String TAG = "Main";
    private final static String ACTION = "com.couchone.libcouch.ICouchService";
    private final static String DBNAME = "contacts";
    private final static String ADMIN_USR = "admin"; // FIXME
    private final static String ADMIN_PWD = "1234";
    private String mHost;
    private int mPort;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);
        startCouch();

        // Buttons
        findViewById(R.id.btn_contacts).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Query RAW contact data
                Cursor c = getContentResolver().query(Data.CONTENT_URI,
                        new String[] { Data._ID, Data.CONTACT_ID, Data.MIMETYPE, Data.DATA1 },
                        null, null, null);
                // Show to output
                while (c.moveToNext()) {
                    ((TextView) findViewById(R.id.output)).append(c.getString(c
                            .getColumnIndex(Data.CONTACT_ID))
                            + ": "
                            + c.getString(c.getColumnIndex(Data.MIMETYPE))
                            + ", "
                            + c.getString(c.getColumnIndex(Data.DATA1)) + "\n");
                }
            }
        });
        findViewById(R.id.btn_rep1).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Server -> Local
                new RequestTask(mHost, mPort, "POST", "_replicate", "{\"source\":\""
                        + getString(R.string.server_master, ADMIN_USR, ADMIN_PWD) + "/" + DBNAME
                        + "\",\"target\":\"" + DBNAME
                        + "\",\"create_target\":true,\"continuous\":true}").execute();
            }
        });
        findViewById(R.id.btn_rep2).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Local -> Server
                new RequestTask(mHost, mPort, "POST", "_replicate", "{\"source\":\"" + DBNAME
                        + "\",\"target\":\""
                        + getString(R.string.server_master, ADMIN_USR, ADMIN_PWD) + "/" + DBNAME
                        + "\"}").execute();
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
            Network res = Network.request(url, method, data);
            if (res != null) {
                return res.result;
            }
            return null;
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
