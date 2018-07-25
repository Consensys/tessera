package com.quorum.tessera.ssl.context;

import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.ssl.exception.TesseraSecurityException;
import com.quorum.tessera.ssl.strategy.TrustMode;
import org.bouncycastle.operator.OperatorCreationException;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;

public class ClientSSLContextFactoryImpl implements ClientSSLContextFactory {

    @Override
    public SSLContext from(SslConfig sslConfig) {

        TrustMode trustMode = TrustMode
            .getValueIfPresent(sslConfig.getClientTrustMode().name())
            .orElse(TrustMode.NONE);

        Path keyStore = sslConfig.getClientKeyStore();
        String keyStorePassword = sslConfig.getClientKeyStorePassword();
        Path trustStore = sslConfig.getClientTrustStore();
        String trustStorePassword = sslConfig.getClientTrustStorePassword();
        Path knownHostsFile = sslConfig.getKnownServersFile();

        try {
            return trustMode
                .createSSLContext(keyStore, keyStorePassword, trustStore, trustStorePassword, knownHostsFile);
        } catch (NoSuchAlgorithmException | KeyManagementException | UnrecoverableKeyException | CertificateException | KeyStoreException | IOException | OperatorCreationException | NoSuchProviderException | InvalidKeyException | SignatureException ex) {
            throw new TesseraSecurityException(ex);
        }
    }
}
