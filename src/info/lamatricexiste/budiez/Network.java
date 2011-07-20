package info.lamatricexiste.budiez;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Network {

    public static String request(URL url, String method, String data) {
        StringBuffer sb = new StringBuffer();
        try {
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            String charEncoding = "iso-8859-1";
            c.setDoOutput(true);
            c.setUseCaches(false);
            c.setRequestMethod(method);
            c.setRequestProperty("Content-type", "application/json; charset=UTF-8");

            if (method != "GET" && data != null) {
                c.setDoInput(true);
                c.setRequestProperty("Content-Length", Integer.toString(data.length()));
                c.getOutputStream().write(data.getBytes(charEncoding));
            }
            c.connect();
            try {
                BufferedReader rd = new BufferedReader(new InputStreamReader(c.getInputStream()));
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
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
