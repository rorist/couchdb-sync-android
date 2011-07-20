package info.lamatricexiste.budiez.authenticator;

import info.lamatricexiste.budiez.Constants;
import info.lamatricexiste.budiez.R;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    public static final String PARAM_CONFIRM_CREDENTIALS = "confirmCredentials";
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_USERNAME = "username";
    private AccountManager mAccountManager;
    private String mUsername;
    private String mPassword;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mAccountManager = AccountManager.get(this);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.login_activity);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Log.e("AuthenticatorActivity", "onCreateDialog()");
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Authenticating");
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setOnCancelListener(null);
        return dialog;
    }

    public void handleLogin(View view) {
        Log.e("AuthenticatorActivity", "handlLogin()");
        mUsername = ((EditText) findViewById(R.id.username_edit)).getText().toString();
        mPassword = ((EditText) findViewById(R.id.password_edit)).getText().toString();
        new AuthTask().execute(mUsername, mPassword);
    }

    private class AuthTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            showDialog(0);
        }

        @Override
        protected String doInBackground(String... params) {
            // Send user auth
            final String user = params[0];
            final String pass = params[1];

            String token = "";
            return token;
        }

        @Override
        protected void onPostExecute(String token) {
            // Create account
            final Account account = new Account(mUsername, Constants.ACCOUNT_TYPE);
            mAccountManager.addAccountExplicitly(account, mPassword, null);

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
