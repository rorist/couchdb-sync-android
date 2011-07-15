package info.lamatricexiste.budiez.syncadapter;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private AccountManager mAccountManager;

    // private Context mContext;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        // mContext = context;
        mAccountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
            ContentProviderClient provider, SyncResult syncResult) {
        Log.e("SyncAdapter", "onPerformSync()");
        try {
            String authtoken = mAccountManager.blockingGetAuthToken(account, "custom_type", true);
            // SYNC HERE
            Log.e("SyncAdapter", authtoken);
        }
        catch (OperationCanceledException e) {
            e.printStackTrace();
        }
        catch (AuthenticatorException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
