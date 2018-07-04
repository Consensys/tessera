package com.github.nexus.socket;

import com.github.nexus.config.ServerConfig;
import com.github.nexus.config.SslConfig;
import com.github.nexus.ssl.strategy.TrustMode;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import java.net.URI;

public class HttpProxyFactory {

    private final URI serverUri;

    private final SocketFactory socketFactory;

    public HttpProxyFactory(final ServerConfig serverConfig) throws Exception {
        this.serverUri = serverConfig.getServerUri();

        if (serverConfig.isSsl()) {
            SslConfig sslConfg = serverConfig.getSslConfig();

            final SSLContext sslContext = TrustMode.NONE.createSSLContext(
                sslConfg.getClientKeyStore().toString(),
                sslConfg.getClientKeyStorePassword(),
                sslConfg.getClientTrustStore().toString(),
                sslConfg.getClientTrustStorePassword(),
                sslConfg.getKnownServersFile().toString()
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
