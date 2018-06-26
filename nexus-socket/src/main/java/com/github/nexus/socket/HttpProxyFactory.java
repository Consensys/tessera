package com.github.nexus.socket;

import com.github.nexus.configuration.Configuration;
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

    private final String knownServer;

    private final TrustMode trustMode;

    public HttpProxyFactory(final Configuration configuration) {
        this.serverUri = configuration.uri();
        this.authenticationMode = AuthenticationMode.getValue(configuration.tls());
        this.keyStore = configuration.clientKeyStore();
        this.keyStorePassword = configuration.clientKeyStorePassword();
        this.trustStore = configuration.clientTrustStore();
        this.trustStorePassword = configuration.clientTrustStorePassword();
        this.trustMode = TrustMode.NONE;
        this.knownServer = configuration.knownServers();
    }

    private HttpProxy createSecureConnection() throws IOException, CertificateException, NoSuchAlgorithmException,
        UnrecoverableKeyException, InvalidKeyException, SignatureException, NoSuchProviderException,
        OperatorCreationException, KeyStoreException, KeyManagementException {

        final SSLContext sslContext = trustMode.createSSLContext(
            keyStore,
            keyStorePassword,
            trustStore,
            trustStorePassword,
            knownServer
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
        OperatorCreationException, NoSuchProviderException, KeyManagementException
    {
        if (AuthenticationMode.STRICT == authenticationMode) {
            return createSecureConnection();
        } else {
            return createInsecureConnection();
        }
    }


}
