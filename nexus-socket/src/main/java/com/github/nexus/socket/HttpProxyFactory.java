package com.github.nexus.socket;

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
import java.util.Objects;

public class HttpProxyFactory {

    private final URI serverUri;

    private AuthenticationMode authenticationMode;

    private String keyStore;

    private String keyStorePassword;

    private String trustStore;

    private String trustStorePassword;

    private String knownServer;

    private TrustMode trustMode;


    protected HttpProxyFactory(final URI serverUri) {
        this.serverUri = Objects.requireNonNull(serverUri);
    }


    public HttpProxyFactory auth(final String authenticationMode) {
        this.authenticationMode = AuthenticationMode.getValue(authenticationMode);
        return this;
    }

    public HttpProxyFactory keyStore(final String keyStore) {
        this.keyStore = keyStore;
        return this;
    }

    public HttpProxyFactory keyStorePassword(final String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
        return this;
    }

    public HttpProxyFactory trustStore(final String trustStore) {
        this.trustStore = trustStore;
        return this;
    }

    public HttpProxyFactory trustStorePassword(final String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
        return this;
    }

    public HttpProxyFactory trustMode(final String trustMode) {
        this.trustMode = TrustMode.getValueIfPresent(trustMode).orElse(TrustMode.NONE);
        return this;
    }

    public HttpProxyFactory knownServers(final String knownServers) {
        this.knownServer = knownServers;
        return this;
    }



    private HttpProxy createSecureConnection() throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, InvalidKeyException, SignatureException, NoSuchProviderException, OperatorCreationException, KeyStoreException, KeyManagementException {

            final SSLContext sslContext =
                trustMode.createSSLContext(
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

    public HttpProxy create() throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, InvalidKeyException, SignatureException, OperatorCreationException, NoSuchProviderException, KeyManagementException {
        if (AuthenticationMode.strict == authenticationMode) {
            return createSecureConnection();
        }
        else {
            return createInsecureConnection();
        }
    }




}
