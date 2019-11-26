package suite;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.io.IOCallback;
import java.net.URL;
import javax.ws.rs.core.UriBuilder;

import com.quorum.tessera.reflect.ReflectCallback;
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

            ServerStatusCheck check = ReflectCallback.execute(() -> {
                return (ServerStatusCheck) Class.forName("suite.GrpcServerStatusCheck")
                    .getConstructor(URL.class, AppType.class)
                    .newInstance(grpcUrl,serverConfig.getApp());
            });
            return check;
        }

        throw new UnsupportedOperationException("Unable to cerate server check for " + serverConfig);
    }
}
