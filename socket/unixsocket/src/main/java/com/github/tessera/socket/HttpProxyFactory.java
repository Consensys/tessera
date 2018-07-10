package com.github.tessera.socket;

import com.github.tessera.config.ServerConfig;
import com.github.tessera.config.SslConfig;
import com.github.tessera.ssl.strategy.TrustMode;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import java.net.URI;

public class HttpProxyFactory {

    private final URI serverUri;

    private final SocketFactory socketFactory;

    public HttpProxyFactory(final ServerConfig serverConfig) throws Exception {
        this.serverUri = serverConfig.getServerUri();

        if (serverConfig.isSsl()) {
            SslConfig sslConfig = serverConfig.getSslConfig();

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

    public HttpProxy create() {
        return new HttpProxy(serverUri, socketFactory);
    }

    
    
    

}
