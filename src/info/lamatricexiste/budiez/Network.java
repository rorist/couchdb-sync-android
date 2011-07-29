package info.lamatricexiste.budiez;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpStatus;

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
    public String error = null;
    public String result = null;
    public int status = HttpStatus.SC_OK;

    public Network(Map<String, List<String>> headers, String result, int status) {
        this.headers = headers;
        this.result = result;
        this.status = status;
    }

    public Network(String error) {
        this.error = error;
    }

    public static Network requestWithCookie(final Context ctxt, URL url, String method,
            String data, HashMap<String, String> headers) {
        // Get cookie token in account manager
        String cookie = "";
        AccountManager mgr = AccountManager.get(ctxt);
        Account[] act = mgr.getAccountsByType(Constants.ACCOUNT_TYPE);
        if (act.length == 0) {
            return null;
        }
        AccountManagerFuture<Bundle> accountManagerFuture = mgr.getAuthToken(act[0],
                Constants.AUTHTOKEN_TYPE, true, null, null);
        try {
            cookie = accountManagerFuture.getResult().getString(AccountManager.KEY_AUTHTOKEN);
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
        Network net = request(url, method, data, headers);

        // Save cookie response
        if (net != null && net.headers != null) {
            String token = null;
            Iterator<Entry<String, List<String>>> it = net.headers.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, List<String>> pairs = it.next();
                if ("set-cookie".equals(pairs.getKey().toLowerCase())) {
                    token = pairs.getValue().get(0);
                }
            }
            if (token != null && !token.equals(cookie)) {
                mgr.setPassword(act[0], token);
            }
        }

        return net;
    }

    public static Network request(URL url, String method, String data) {
        return request(url, method, data, null);
    }

    public static Network request(URL url, String method, String data,
            HashMap<String, String> headers) {
        try {
            // Do request
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            String charEncoding = "iso-8859-1";
            c.setDoOutput(true);
            c.setUseCaches(false);
            c.setRequestMethod(method);
            c.setRequestProperty("Content-type", "application/json; charset=UTF-8");
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    c.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            if (!"GET".equals(method) && !"DELETE".equals(method) && data != null) {
                c.setDoInput(true);
                c.setRequestProperty("Content-Length", Integer.toString(data.length()));
                c.getOutputStream().write(data.getBytes(charEncoding));
            }
            c.connect();

            // Get response
            final StringBuffer sb = new StringBuffer();
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
            catch (StringIndexOutOfBoundsException e) {
                // FIXME: Android bug: http://code.google.com/p/android/issues/detail?id=18856
                return new Network(null, null, 401);
            }
            finally {
                c.disconnect();
            }
            return new Network(c.getHeaderFields(), sb.toString(), c.getResponseCode());
        }
        catch (IOException e) {
            e.printStackTrace();
            return new Network(e.getLocalizedMessage());
        }
    }
}
