package com.quorum.tessera.config;

import com.quorum.tessera.config.adapters.PathAdapter;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.nio.file.Path;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class SslConfig {

    @NotNull
    @XmlElement(required = true)
    private final SslAuthenticationMode tls;

    @XmlElement(defaultValue = "false")
    private final boolean generateKeyStoreIfNotExisted;

    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path serverKeyStore;

    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path serverTlsKeyPath;

    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path serverTlsCertificatePath;

    @XmlElement
    private final String serverKeyStorePassword;

    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path serverTrustStore;

    @XmlElement
    @XmlJavaTypeAdapter(value = PathAdapter.class)
    private final List<Path> serverTrustCertificates;

    @XmlElement
    private final String serverTrustStorePassword;

    @XmlElement
    private final SslTrustMode serverTrustMode;

    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path clientKeyStore;

    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path clientTlsKeyPath;

    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path clientTlsCertificatePath;

    @XmlElement
    private final String clientKeyStorePassword;

    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path clientTrustStore;

    @XmlElement
    @XmlJavaTypeAdapter(value = PathAdapter.class)
    private final List<Path> clientTrustCertificates;

    @XmlElement
    private final String clientTrustStorePassword;

    @XmlElement
    private final SslTrustMode clientTrustMode;

    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path knownClientsFile;

    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path knownServersFile;

    public SslConfig(
            SslAuthenticationMode tls,
            boolean generateKeyStoreIfNotExisted,
            Path serverKeyStore,
            String serverKeyStorePassword,
            Path serverTrustStore,
            String serverTrustStorePassword,
            SslTrustMode serverTrustMode,
            Path clientKeyStore,
            String clientKeyStorePassword,
            Path clientTrustStore,
            String clientTrustStorePassword,
            SslTrustMode clientTrustMode,
            Path knownClientsFile,
            Path knownServersFile,
            List<Path> serverTrustCertificates,
            List<Path> clientTrustCertificates,
            Path serverTlsKeyPath,
            Path serverTlsCertificatePath,
            Path clientTlsKeyPath,
            Path clientTlsCertificatePath
    ) {

        this.tls = tls;
        this.generateKeyStoreIfNotExisted = generateKeyStoreIfNotExisted;
        this.serverKeyStore = serverKeyStore;
        this.serverTrustCertificates = serverTrustCertificates;
        this.serverKeyStorePassword = serverKeyStorePassword;
        this.serverTrustStore = serverTrustStore;
        this.serverTrustStorePassword = serverTrustStorePassword;
        this.serverTrustMode = serverTrustMode;
        this.clientKeyStore = clientKeyStore;
        this.clientTrustCertificates = clientTrustCertificates;
        this.clientKeyStorePassword = clientKeyStorePassword;
        this.clientTrustStore = clientTrustStore;
        this.clientTrustStorePassword = clientTrustStorePassword;
        this.clientTrustMode = clientTrustMode;
        this.knownClientsFile = knownClientsFile;
        this.knownServersFile = knownServersFile;
        this.serverTlsKeyPath = serverTlsKeyPath;
        this.serverTlsCertificatePath = serverTlsCertificatePath;
        this.clientTlsKeyPath = clientTlsKeyPath;
        this.clientTlsCertificatePath = clientTlsCertificatePath;

    }

    private SslConfig() {
        this(null, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null,null,null,null,null);
    }

    private static SslConfig create() {
        return new SslConfig();
    }

    public SslAuthenticationMode getTls() {
        return tls;
    }

    public boolean isGenerateKeyStoreIfNotExisted() {
        return generateKeyStoreIfNotExisted;
    }

    public Path getServerKeyStore() {
        return serverKeyStore;
    }

    public String getServerKeyStorePassword() {
        return serverKeyStorePassword;
    }

    public Path getServerTrustStore() {
        return serverTrustStore;
    }

    public String getServerTrustStorePassword() {
        return serverTrustStorePassword;
    }

    public SslTrustMode getServerTrustMode() {
        return serverTrustMode;
    }

    public Path getClientKeyStore() {
        return clientKeyStore;
    }

    public String getClientKeyStorePassword() {
        return clientKeyStorePassword;
    }

    public Path getClientTrustStore() {
        return clientTrustStore;
    }

    public String getClientTrustStorePassword() {
        return clientTrustStorePassword;
    }

    public SslTrustMode getClientTrustMode() {
        return clientTrustMode;
    }

    public Path getKnownClientsFile() {
        return knownClientsFile;
    }

    public Path getKnownServersFile() {
        return knownServersFile;
    }

    public List<Path> getServerTrustCertificates() {
        return serverTrustCertificates;
    }

    public List<Path> getClientTrustCertificates() {
        return clientTrustCertificates;
    }

    public Path getServerTlsKeyPath() {
        return serverTlsKeyPath;
    }

    public Path getServerTlsCertificatePath() {
        return serverTlsCertificatePath;
    }

    public Path getClientTlsKeyPath() {
        return clientTlsKeyPath;
    }

    public Path getClientTlsCertificatePath() {
        return clientTlsCertificatePath;
    }

}
