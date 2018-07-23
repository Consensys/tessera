package com.github.tessera.socket;

import com.github.tessera.config.ServerConfig;
import com.github.tessera.config.SslConfig;
import com.github.tessera.ssl.strategy.TrustMode;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import java.net.URI;

/**
 * Creates proxies to the local HTTP server, setting up connection
 * configuration such as the target address and SSL settings.
 */
public class HttpProxyFactory {

    private final URI serverUri;

    private final SocketFactory socketFactory;

    public HttpProxyFactory(final ServerConfig serverConfig) throws Exception {
        this.serverUri = serverConfig.getServerUri();

        if (serverConfig.isSsl()) {

            final SslConfig sslConfig = serverConfig.getSslConfig();

            final SSLContext sslContext = TrustMode.NONE.createSSLContext(
                sslConfig.getClientKeyStore(),
                sslConfig.getClientKeyStorePassword(),
                sslConfig.getClientTrustStore(),
                sslConfig.getClientTrustStorePassword(),
                sslConfig.getKnownServersFile()
            );
            
            this.socketFactory = sslContext.getSocketFactory();
        } else {
            this.socketFactory = SocketFactory.getDefault();
        }

    }

    /**
     * Creates a new proxy to the server, whose connection hasn't yet been opened
     *
     * @return
     */
    public HttpProxy create() {
        return new HttpProxy(serverUri, socketFactory);
    }

}
