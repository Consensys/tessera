package com.github.nexus.socket;

import com.github.nexus.configuration.Configuration;
import com.github.nexus.ssl.strategy.AuthenticationMode;
import com.github.nexus.ssl.strategy.TrustMode;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import java.net.URI;

public class HttpProxyFactory {

    private final URI serverUri;

    private final SocketFactory socketFactory;

    public HttpProxyFactory(final Configuration configuration) throws Exception {
        this.serverUri = configuration.uri();

        final AuthenticationMode authenticationMode = AuthenticationMode.getValue(configuration.tls());

        if (AuthenticationMode.strict.equals(authenticationMode)) {

            final SSLContext sslContext = TrustMode.NONE.createSSLContext(
                configuration.clientKeyStore(),
                configuration.clientKeyStorePassword(),
                configuration.clientTrustStore(),
                configuration.clientTrustStorePassword(),
                configuration.knownServers()
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
