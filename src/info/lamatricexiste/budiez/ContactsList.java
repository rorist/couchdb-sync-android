package info.lamatricexiste.budiez;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.widget.SimpleAdapter;

public class ContactsList extends ListActivity {

    private final static String ACTION_LIST = "contacts/_design/read/_view/titles";
    private final static String NAME = "title";
    private final static String ID = "id";
    private final ArrayList<HashMap<String, String>> mList = new ArrayList<HashMap<String, String>>();
    private SimpleAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.list);

        // Get data
        Intent intent = getIntent();
        String action = intent.getAction();
        if (Constants.ACTION_LOCAL_LIST.equals(action)) {
            Bundle data = intent.getExtras();
            // Server -> Local
            String host = data.getString(Constants.DATA_HOST);
            int port = data.getInt(Constants.DATA_PORT);
            new LocalRequestTask(host, port, "GET", ACTION_LIST, "", null).execute();
        } else {
            new RemoteRequestTask(ContactsList.this, "GET", ACTION_LIST, "", null).execute();
        }

        // Show data
        mAdapter = new SimpleAdapter(ContactsList.this, mList, android.R.layout.two_line_list_item,
                new String[] { NAME, ID }, new int[] { android.R.id.text1, android.R.id.text2 });
        setListAdapter(mAdapter);
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

        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
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
                // Handle errors
                if (net.status != HttpStatus.SC_OK) {
                    AccountManager mgr = AccountManager.get(ContactsList.this);
                    Account[] act = mgr.getAccountsByType(Constants.ACCOUNT_TYPE);
                    if (act.length >= 1) {
                        mgr.confirmCredentials(act[0], null, ContactsList.this, null, null);
                        finish();
                    }
                } else {
                    try {
                        // Handle results
                        JSONObject res = new JSONObject(net.result);
                        JSONArray rows = res.getJSONArray("rows");
                        for (int i = 0; i < rows.length(); i++) {
                            HashMap<String, String> entry = new HashMap<String, String>(1);
                            JSONObject contact = rows.getJSONObject(i).getJSONObject("value");
                            entry.put(NAME, contact.getString("title"));
                            entry.put(ID, contact.getString("id"));
                            mList.add(entry);
                        }
                        mAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        protected void onCancelled() {
            setProgressBarIndeterminateVisibility(false);
        }

    }

    private class LocalRequestTask extends AsyncTask<Void, Void, Network> {

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
        protected Network doInBackground(Void... params) {
            Network res = Network.request(url, method, data, headers);
            if (res != null) {
                if (res.error != null) {
                    return null;
                } else {
                    return res;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Network net) {
            setProgressBarIndeterminateVisibility(false);
            if (net == null) {
                return;
            }
            try {
                // Handle results
                JSONObject res = new JSONObject(net.result);
                JSONArray rows = res.getJSONArray("rows");
                for (int i = 0; i < rows.length(); i++) {
                    HashMap<String, String> entry = new HashMap<String, String>(1);
                    JSONObject contact = rows.getJSONObject(i).getJSONObject("value");
                    entry.put(NAME, contact.getString("title"));
                    entry.put(ID, contact.getString("id"));
                    mList.add(entry);
                }
                mAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            setProgressBarIndeterminateVisibility(false);
        }

    }
}
