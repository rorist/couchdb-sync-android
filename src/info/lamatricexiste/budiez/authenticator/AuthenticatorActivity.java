package info.lamatricexiste.budiez.authenticator;

import info.lamatricexiste.budiez.Constants;
import info.lamatricexiste.budiez.R;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    public static final String PARAM_CONFIRM_CREDENTIALS = "confirmCredentials";
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_USERNAME = "username";
    private AccountManager mAccountManager;

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
        final String mUsername = "rorist";
        final String mPassword = "1be168ff837f043bde17c0314341c84271047b31";
        final Account account = new Account(mUsername, Constants.ACCOUNT_TYPE);
        mAccountManager.addAccountExplicitly(account, mPassword, null);

        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUsername);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, mPassword);
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);

        finish();
    }

}
