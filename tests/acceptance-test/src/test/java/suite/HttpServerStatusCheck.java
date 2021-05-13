package suite;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.ssl.context.ClientSSLContextFactoryImpl;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Optional;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerStatusCheck implements ServerStatusCheck {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerStatusCheck.class);

  private final URI uri;

  private final AppType appType;

  private final SSLContext sslContext;

  public HttpServerStatusCheck(AppType appType, URI uri) {
    this(appType, uri, null);
  }

  public HttpServerStatusCheck(AppType appType, URI uri, SslConfig sslConfig) {
    this.uri = uri;
    this.appType = appType;
    sslContext =
        Optional.ofNullable(sslConfig)
            .map(
                config -> {
                  return new ClientSSLContextFactoryImpl().from(uri.toString(), config);
                })
            .orElse(null);
  }

  @Override
  public boolean checkStatus() {

    HttpURLConnection httpConnection = null;
    try {

      httpConnection = (HttpURLConnection) uri.toURL().openConnection();

      if (sslContext != null) {
        HttpsURLConnection.class
            .cast(httpConnection)
            .setSSLSocketFactory(sslContext.getSocketFactory());
      }

      httpConnection.connect();

      return true;
    } catch (IOException ex) {
      LOGGER.warn("appType: {} url: {}, message: {}", appType, uri, ex.getMessage());
      LOGGER.debug(null, ex);
      return false;
    } finally {
      if (httpConnection != null) {
        httpConnection.disconnect();
      }
    }
  }

  @Override
  public String toString() {
    return "HttpsServerStatusCheck{" + "url=" + uri + '}';
  }
}
