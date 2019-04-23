package suite;

import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.ssl.context.ClientSSLContextFactoryImpl;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URL;

public class HttpsServerStatusCheck implements ServerStatusCheck {

    private final URL url;

    private final SslConfig sslConfig;

    public HttpsServerStatusCheck(URL url, SslConfig sslConfig) {
        this.url = url;
        this.sslConfig = sslConfig;
    }

    @Override
    public boolean checkStatus() {
        HttpsURLConnection httpsConnection = null;
        try {
            httpsConnection = (HttpsURLConnection) url.openConnection();
            SSLContext sslContext = new ClientSSLContextFactoryImpl()
                .from(url.toString(), sslConfig);
            httpsConnection.setSSLSocketFactory(sslContext.getSocketFactory());

            httpsConnection.connect();

            return true;
        } catch (IOException ex) {
            LOGGER.warn(ex.getMessage());
            LOGGER.debug(null, ex);
            return false;
        } finally {
            if (httpsConnection != null) {
                httpsConnection.disconnect();
            }
        }

    }

    @Override
    public String toString() {
        return "HttpsServerStatusCheck{" + "url=" + url + '}';
    }

}
