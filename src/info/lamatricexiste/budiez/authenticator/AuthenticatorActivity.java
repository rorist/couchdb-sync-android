package info.lamatricexiste.budiez.authenticator;

import info.lamatricexiste.budiez.Constants;
import info.lamatricexiste.budiez.Network;
import info.lamatricexiste.budiez.R;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    // public static final String TAG = "AuthenticatorActivity";
    public static final String PARAM_CONFIRM_CREDENTIALS = "confirmCredentials";
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_USERNAME = "username";
    private AccountManager mAccountManager;
    private String mUsername;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mAccountManager = AccountManager.get(this);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.login_activity);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Authenticating");
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setOnCancelListener(null);
        return dialog;
    }

    public void handleLogin(View view) {
        mUsername = ((EditText) findViewById(R.id.username_edit)).getText().toString();
        String password = ((EditText) findViewById(R.id.password_edit)).getText().toString();
        new AuthTask().execute(mUsername, password);
    }

    private class AuthTask extends AsyncTask<String, Void, String> {

        private String mServerUrl;

        @Override
        protected void onPreExecute() {
            showDialog(0);
            mServerUrl = getString(R.string.server_master);
        }

        @Override
        protected String doInBackground(String... params) {
            String token = null;
            final String user = params[0];
            final String pass = params[1];
            try {
                // Make request
                final URL url = new URL(String.format(mServerUrl, user, pass) + "/_session");
                final HashMap<String, String> headers = new HashMap<String, String>(1);
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                Network res = Network.request(url, "POST", "name=" + user + "&password=" + pass,
                        headers);
                // Get Cookie
                if (res != null && res.headers != null) {
                    Iterator<Entry<String, List<String>>> it = res.headers.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, List<String>> pairs = it.next();
                        if ("set-cookie".equals(pairs.getKey().toLowerCase())) {
                            token = pairs.getValue().get(0);
                        }
                    }
                }
            }
            catch (MalformedURLException e) {}
            return token;
        }

        @Override
        protected void onPostExecute(String token) {
            // Create account
            final Account account = new Account(mUsername, Constants.ACCOUNT_TYPE);
            mAccountManager.addAccountExplicitly(account, token, null);

            // Set result intention
            final Intent intent = new Intent();
            intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUsername);
            intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
            intent.putExtra(AccountManager.KEY_AUTHTOKEN, token);
            setAccountAuthenticatorResult(intent.getExtras());
            setResult(RESULT_OK, intent);

            // Close Authenticator
            finish();
            dismissDialog(0);
        }

        @Override
        protected void onCancelled() {
            dismissDialog(0);
            // TODO: handle error
        }

    }

}
