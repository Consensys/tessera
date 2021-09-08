package suite;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.io.IOCallback;
import jakarta.ws.rs.core.UriBuilder;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ServerStatusCheck {

  Logger LOGGER = LoggerFactory.getLogger(ServerStatusCheck.class);

  boolean checkStatus();

  static ServerStatusCheck create(ServerConfig serverConfig) {
    CommunicationType communicationType = serverConfig.getCommunicationType();

    if (communicationType == CommunicationType.REST) {

      if (serverConfig.isUnixSocket()) {
        return new UnixSocketServerStatusCheck(serverConfig.getServerUri());
      } else {
        final URI url =
            IOCallback.execute(
                () -> UriBuilder.fromUri(serverConfig.getServerUri()).path("upcheck").build());
        if (serverConfig.isSsl()) {
          return new HttpServerStatusCheck(serverConfig.getApp(), url, serverConfig.getSslConfig());
        } else {
          return new HttpServerStatusCheck(serverConfig.getApp(), url);
        }
      }
    }

    throw new UnsupportedOperationException("Unable to cerate server check for " + serverConfig);
  }
}
