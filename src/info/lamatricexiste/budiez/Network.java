package info.lamatricexiste.budiez;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class Network {

    public Map<String, List<String>> headers;
    public String result;
    public int status;

    public Network(Map<String, List<String>> headers, String result, int status) {
        this.headers = headers;
        this.result = result;
        this.status = status;
    }

    public static Network request(URL url, String method, String data) {
        return request(url, method, data, null);
    }

    public static Network request(URL url, String method, String data, String headers[][]) {
        StringBuffer sb = new StringBuffer();
        try {
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            String charEncoding = "iso-8859-1";
            c.setDoOutput(true);
            c.setUseCaches(false);
            c.setRequestMethod(method);
            c.setRequestProperty("Content-type", "application/json; charset=UTF-8");

            for (String[] tmp : headers) {
                c.setRequestProperty(tmp[0], tmp[1]);
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
