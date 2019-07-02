package suite;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpServerStatusCheck implements ServerStatusCheck {

    private final URL url;

    public HttpServerStatusCheck(URL url) {
        this.url = url;
    }

    @Override
    public boolean checkStatus() {
        HttpURLConnection conn = null;
        try {

            conn = (HttpURLConnection) url.openConnection();
            conn.connect();

            return true;
        } catch (IOException ex) {
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    @Override
    public String toString() {
        return "HttpServerStatusCheck{" + "url=" + url + '}';
    }
}
