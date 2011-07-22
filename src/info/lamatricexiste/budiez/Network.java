package info.lamatricexiste.budiez;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public class Network {

    public static final String TAG = "Network";
    public Map<String, List<String>> headers;
    public String result;
    public int status;

    public Network(Map<String, List<String>> headers, String result, int status) {
        this.headers = headers;
        this.result = result;
        this.status = status;
    }

    public static Network requestWithCookie(final Context ctxt, URL url, String method,
            String data, HashMap<String, String> headers) {
        // Get cookie token in account manager
        String cookie = "";
        AccountManager mgr = AccountManager.get(ctxt); // TODO: Check context
        Account[] act = mgr.getAccountsByType(Constants.ACCOUNT_TYPE);
        if (act.length < 1) { // FIXME: add new account
            return null;
        }
        AccountManagerFuture<Bundle> accountManagerFuture = mgr.getAuthToken(act[0],
                Constants.AUTHTOKEN_TYPE, true, null, null);
        try {
            Bundle authTokenBundle = accountManagerFuture.getResult();
            cookie = authTokenBundle.getString(AccountManager.KEY_AUTHTOKEN);
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

        // Set cookie in headers
        Log.e(TAG, "cookie=" + cookie);
        if (headers == null) {
            headers = new HashMap<String, String>(3);
        }
        headers.put("Cookie", cookie);
        headers.put("X-CouchDB-WWW-Authenticate", "Cookie");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        return request(url, method, data, headers);
    }

    public static Network request(URL url, String method, String data) {
        return request(url, method, data, null);
    }

    public static Network request(URL url, String method, String data,
            HashMap<String, String> headers) {
        StringBuffer sb = new StringBuffer();
        try {
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            String charEncoding = "iso-8859-1";
            c.setDoOutput(true);
            c.setUseCaches(false);
            c.setRequestMethod(method);
            c.setRequestProperty("Content-type", "application/json; charset=UTF-8");

            for (Map.Entry<String, String> entry : headers.entrySet()) {
                c.setRequestProperty(entry.getKey(), entry.getValue());
            }

            if (method != "GET" && data != null) {
                c.setDoInput(true);
                c.setRequestProperty("Content-Length", Integer.toString(data.length()));
                c.getOutputStream().write(data.getBytes(charEncoding));
            }
            c.connect();
            try {
                BufferedReader rd = new BufferedReader(new InputStreamReader(c.getInputStream()),
                        1024);
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                rd.close();
            }
            catch (FileNotFoundException e) {}
            catch (NullPointerException e) {}
            finally {
                c.disconnect();
            }
            return new Network(c.getHeaderFields(), sb.toString(), c.getResponseCode());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
