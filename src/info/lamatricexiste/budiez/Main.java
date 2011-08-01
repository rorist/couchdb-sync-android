package info.lamatricexiste.budiez;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.apache.http.HttpStatus;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.couchbase.libcouch.CouchDB;
import com.couchbase.libcouch.ICouchClient;

public class Main extends Activity {

    private final static String DBNAME = "contacts";
    private final static String ADMIN_USR = "admin"; // FIXME
    private final static String ADMIN_PWD = "1234";
    private ServiceConnection couchServiceConnection;
    private String mHost;
    private int mPort;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);

        // Start service
        setProgressBarIndeterminate(true);
        startCouch();

        // Buttons
        findViewById(R.id.btn_status).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new RemoteRequestTask(Main.this, "GET", "_session").execute();
            }
        });
        findViewById(R.id.btn_login).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new RemoteRequestTask(Main.this, "POST", "_session").execute();
            }
        });
        findViewById(R.id.btn_logout).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new RemoteRequestTask(Main.this, "DELETE", "_session").execute();
            }
        });
        findViewById(R.id.btn_readdb).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new RemoteRequestTask(Main.this, "GET", "contacts", "{}", null).execute();
            }
        });
        findViewById(R.id.btn_remote).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Main.this, ContactsList.class));
            }
        });
        findViewById(R.id.btn_local).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, ContactsList.class);
                intent.setAction(Constants.ACTION_LOCAL_LIST);
                Bundle data = new Bundle();
                data.putString(Constants.DATA_HOST, mHost);
                data.putInt(Constants.DATA_PORT, mPort);
                startActivity(intent);
            }
        });
        // findViewById(R.id.btn_contacts).setOnClickListener(new
        // OnClickListener() {
        // @Override
        // public void onClick(View v) {
        // TextView tv = (TextView) findViewById(R.id.output);
        // tv.setText("");
        // // Query RAW contact data
        // Cursor c = getContentResolver().query(Data.CONTENT_URI,
        // new String[] { Data._ID, Data.CONTACT_ID, Data.MIMETYPE, Data.DATA1
        // },
        // null, null, null);
        // // Show to output
        // while (c.moveToNext()) {
        // tv.append(c.getString(c.getColumnIndex(Data.CONTACT_ID)) + ": "
        // + c.getString(c.getColumnIndex(Data.MIMETYPE)) + ", "
        // + c.getString(c.getColumnIndex(Data.DATA1)) + "\n");
        // }
        // }
        // });
        findViewById(R.id.btn_rep1).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Server -> Local
                new LocalRequestTask(mHost, mPort, "POST", "_replicate", "{\"source\":\""
                        + getString(R.string.server_master, ADMIN_USR, ADMIN_PWD, DBNAME)
                        + "\",\"target\":\"" + DBNAME
                        + "\",\"create_target\":true,\"continuous\":true}", null).execute();
            }
        });
        // findViewById(R.id.btn_rep2).setOnClickListener(new OnClickListener()
        // {
        // @Override
        // public void onClick(View v) {
        // // Local -> Server
        // new LocalRequestTask(mHost, mPort, "POST", "_replicate",
        // "{\"source\":\"" + DBNAME
        // + "\",\"target\":\""
        // + getString(R.string.server_master, ADMIN_USR, ADMIN_PWD, DBNAME) +
        // "\"}",
        // null).execute();
        // }
        // });
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
        } catch (IllegalArgumentException e) {
        }
    }

    private void startCouch() {
        couchServiceConnection = CouchDB.getService(getBaseContext(), null, "release-0.1",
                couchClient);
    }

    private ICouchClient couchClient = new ICouchClient.Stub() {

        private ProgressDialog mDialog = null;

        @Override
        public void couchStarted(String host, int port) throws RemoteException {
            Log.e("Main", "host=" + host + ", port=" + port);
            setProgressBarIndeterminate(false);
            mHost = host;
            mPort = port;
        }

        @Override
        public void exit(String error) throws RemoteException {
            Log.e("EXIT", error);
        }

        @Override
        public void installing(int completed, int total) throws RemoteException {
            if (mDialog == null) {
                mDialog = ProgressDialog.show(Main.this, "CouchDB Installation", completed + " / "
                        + total);
                mDialog.setMax(total);
                mDialog.setCancelable(false);
            }
            mDialog.setMessage(completed + " / " + total);
            mDialog.setProgress(completed);
            if (completed == total - 1) {
                mDialog.dismiss();
            }
        }

    };

    private class LocalRequestTask extends AsyncTask<Void, Void, String> {

        private URL url;
        private String method;
        private String data;
        private HashMap<String, String> headers;

        LocalRequestTask(String host, int port, String method, String action, String data,
                HashMap<String, String> headers) {
            this.method = method;
            this.data = data;
            this.headers = headers;
            try {
                url = new URL("http://" + host + ":" + port + "/" + action);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected String doInBackground(Void... params) {
            Network res = Network.request(url, method, data, headers);
            if (res != null) {
                if (res.error != null) {
                    return res.error;
                } else {
                    return res.result;
                }
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
            ((TextView) findViewById(R.id.output)).append("Cancelled\n");
        }

    }

    private class RemoteRequestTask extends AsyncTask<Void, Void, Network> {

        private URL url;
        private String method;
        private String data;
        private HashMap<String, String> headers;
        private WeakReference<Activity> mActivity;

        RemoteRequestTask(Activity ctxt, String user, String pass, String method, String action,
                String data, HashMap<String, String> headers) {
            mActivity = new WeakReference<Activity>(ctxt);
            this.method = method;
            this.data = data;
            this.headers = headers;
            try {
                url = new URL(getString(R.string.server_master, user, pass, action));
            } catch (MalformedURLException e) {
            }
        }

        RemoteRequestTask(Activity ctxt, String method, String action, String data,
                HashMap<String, String> headers) {
            this(ctxt, "", "", method, action, data, headers);
        }

        RemoteRequestTask(Activity ctxt, String method, String action) {
            this(ctxt, "", "", method, action, null, null);
        }

        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
            // Check for an existing account first
            AccountManager mgr = AccountManager.get(Main.this);
            Account[] act = mgr.getAccountsByType(Constants.ACCOUNT_TYPE);
            if (act.length == 0) {
                mgr.addAccount(Constants.ACCOUNT_TYPE, Constants.AUTHTOKEN_TYPE, null, null,
                        Main.this, null, null);
            }
        }

        @Override
        protected Network doInBackground(Void... params) {
            final Activity a = mActivity.get();
            if (a != null) {
                return Network.requestWithCookie(a.getApplicationContext(), url, method, data,
                        headers);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Network net) {
            setProgressBarIndeterminateVisibility(false);
            if (net != null) {
                ((TextView) findViewById(R.id.output)).setText(net.result);
                // Handle errors
                if (net.status != HttpStatus.SC_OK) {
                    AccountManager mgr = AccountManager.get(Main.this);
                    Account[] act = mgr.getAccountsByType(Constants.ACCOUNT_TYPE);
                    mgr.confirmCredentials(act[0], null, Main.this, null, null);
                    // execute(); // FIXME: Execute request again
                }
            }
        }

        @Override
        protected void onCancelled() {
            setProgressBarIndeterminateVisibility(false);
            ((TextView) findViewById(R.id.output)).append("Cancelled\n");
        }

    }
}
