package com.quorum.tessera.ssl.context.model;

import java.nio.file.Path;
import java.util.List;

public class SSLContextProperties {

    private String address;
    private Path keyStore;
    private String keyStorePassword;
    private Path key;
    private Path certificate;
    private Path trustStore;
    private String trustStorePassword;
    private List<Path> trustedCertificates;
    private Path knownHosts;

    public SSLContextProperties(String address,
                                Path keyStore,
                                String keyStorePassword,
                                Path key,
                                Path certificate,
                                Path trustStore,
                                String trustStorePassword,
                                List<Path> trustedCertificates,
                                Path knownHosts) {
        this.address = address;
        this.keyStore = keyStore;
        this.keyStorePassword = keyStorePassword;
        this.key = key;
        this.certificate = certificate;
        this.trustStore = trustStore;
        this.trustStorePassword = trustStorePassword;
        this.trustedCertificates = trustedCertificates;
        this.knownHosts = knownHosts;
    }

    public String getAddress() {
        return address;
    }

    public Path getKeyStore() {
        return keyStore;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public Path getKey() {
        return key;
    }

    public Path getCertificate() {
        return certificate;
    }

    public Path getTrustStore() {
        return trustStore;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public List<Path> getTrustedCertificates() {
        return trustedCertificates;
    }

    public Path getKnownHosts() {
        return knownHosts;
    }
}
