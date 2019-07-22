package suite;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.io.IOCallback;
import java.net.URL;
import javax.ws.rs.core.UriBuilder;
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
                final URL url =
                        IOCallback.execute(
                                () -> UriBuilder.fromUri(serverConfig.getServerUri()).path("upcheck").build().toURL());
                if (serverConfig.isSsl()) {
                    return new HttpsServerStatusCheck(url, serverConfig.getSslConfig());
                } else {
                    return new HttpServerStatusCheck(url);
                }
            }
        }

        if (communicationType == CommunicationType.GRPC) {
            URL grpcUrl =
                    IOCallback.execute(
                            () -> UriBuilder.fromUri(serverConfig.getBindingUri()).path("upcheck").build().toURL());
            return new GrpcServerStatusCheck(grpcUrl, serverConfig.getApp());
        }

        throw new UnsupportedOperationException("Unable to cerate server check for " + serverConfig);
    }
}
