package info.lamatricexiste.budiez;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;
import org.json.JSONArray;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.Data;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class ContactsList extends ListActivity {

    private final static String NAME = "name";
    private final ArrayList<HashMap<String, String>> mList = new ArrayList<HashMap<String, String>>();
    private ListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);

        // Get data
        new RemoteRequestTask(ContactsList.this, "GET", "contacts/_all_docs", "", null).execute();

        // Show data
        mAdapter = new SimpleAdapter(ContactsList.this, list, android.R.id.list, new String[] { NAME }, new int[] { android.R.id.text1 });
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
            }
            catch (MalformedURLException e) {}
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
                ((TextView) findViewById(R.id.output)).setText(net.result);
                // Handle errors
                if (net.status != HttpStatus.SC_OK) {
                    AccountManager mgr = AccountManager.get(ContactsList.this);
                    Account[] act = mgr.getAccountsByType(Constants.ACCOUNT_TYPE);
                    if (act.length >= 1) {
                        mgr.confirmCredentials(act[0], null, ContactsList.this, null, null);
                        // TODO: Do request again
                    }
                } else {
                    // Handle results
                    JSONObject res = new JSONObject(net.result);
                    JSONArray rows = res.getJSONArray("rows");
                    for(int i=0; i<rows.length(); i++){
                        mList.add(new HashMap<String, String>());
                    }
                    mAdapter.notifyDataSetChanged();
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
