package com.github.nexus.socket;

import com.github.nexus.config.ServerConfig;
import com.github.nexus.config.SslConfig;
import com.github.nexus.ssl.strategy.AuthenticationMode;
import com.github.nexus.ssl.strategy.TrustMode;
import org.bouncycastle.operator.OperatorCreationException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.URI;
import java.security.*;
import java.security.cert.CertificateException;

public class HttpProxyFactory {

    private final URI serverUri;

    private final AuthenticationMode authenticationMode;

    private final String keyStore;

    private final String keyStorePassword;

    private final String trustStore;

    private final String trustStorePassword;

    private final String knownServers;

    private final TrustMode trustMode;

    public HttpProxyFactory(final ServerConfig serverConfig) {
        this.serverUri = serverConfig.getServerUri();

        if (serverConfig.isSsl()) {
            SslConfig sslConfg = serverConfig.getSslConfig();

            this.authenticationMode
                    = AuthenticationMode.getValue(sslConfg.getClientTrustMode().name());

            this.keyStore = sslConfg.getClientTrustStore().toString();
            this.keyStorePassword = sslConfg.getClientKeyStorePassword();
            this.trustStore = sslConfg.getClientTrustStore().toString();
            this.trustStorePassword = sslConfg.getClientTrustStorePassword();
            this.trustMode = TrustMode.NONE;
            this.knownServers = sslConfg.getKnownServersFile().toString();
        } else {
            this.authenticationMode = AuthenticationMode.OFF;

            this.keyStore = null;
            this.keyStorePassword = null;
            this.trustStore = null;
            this.trustStorePassword = null;
            this.trustMode = TrustMode.NONE;
            this.knownServers = null;
        }

    }

    private HttpProxy createSecureConnection() throws IOException, CertificateException, NoSuchAlgorithmException,
            UnrecoverableKeyException, InvalidKeyException, SignatureException, NoSuchProviderException,
            OperatorCreationException, KeyStoreException, KeyManagementException {

        final SSLContext sslContext = trustMode.createSSLContext(
                keyStore,
                keyStorePassword,
                trustStore,
                trustStorePassword,
                knownServers
        );

        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        return new HttpProxy(serverUri, sslSocketFactory);
    }

    private HttpProxy createInsecureConnection() {
        final SocketFactory socketFactory = SocketFactory.getDefault();

        return new HttpProxy(serverUri, socketFactory);
    }

    public HttpProxy create() throws IOException, CertificateException, NoSuchAlgorithmException,
            UnrecoverableKeyException, KeyStoreException, InvalidKeyException, SignatureException,
            OperatorCreationException, NoSuchProviderException, KeyManagementException {
        if (AuthenticationMode.STRICT == authenticationMode) {
            return createSecureConnection();
        } else {
            return createInsecureConnection();
        }
    }

}
