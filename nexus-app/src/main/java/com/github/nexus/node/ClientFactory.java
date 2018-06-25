package com.github.nexus.node;

import com.github.nexus.ssl.strategy.AuthenticationMode;
import com.github.nexus.ssl.strategy.TrustMode;
import org.bouncycastle.operator.OperatorCreationException;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class ClientFactory {

    public static Client buildClient(String secure, String keyStore, String keyStorePassword, String trustStore,
                                     String trustStorePassword, String trustMode, String knownServers) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, OperatorCreationException, NoSuchProviderException, InvalidKeyException, SignatureException {
        if (AuthenticationMode.strict == AuthenticationMode.getValue(secure)){
            return buildSecureClient(keyStore, keyStorePassword, trustStore, trustStorePassword, trustMode, knownServers);
        }
        else {
            return buildInsecureClient();
        }
    }

    private static Client buildInsecureClient(){
        return ClientBuilder.newClient();
    }

    private static Client buildSecureClient(String keyStore, String keyStorePassword, String trustStore,
                                           String trustStorePassword, String trustMode, String knownServers) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, OperatorCreationException, NoSuchProviderException, InvalidKeyException, SignatureException {

        final SSLContext sslContext = TrustMode
            .getValueIfPresent(trustMode)
            .orElse(TrustMode.NONE)
            .createSSLContext(keyStore,keyStorePassword,trustStore,trustStorePassword,knownServers);

        return ClientBuilder.newBuilder()
            .sslContext(sslContext)
            .build();
    }

}
