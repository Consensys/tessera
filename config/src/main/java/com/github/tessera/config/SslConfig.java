package com.github.tessera.config;

import com.github.tessera.config.adapters.PathAdapter;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.nio.file.Path;

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

    @XmlElement
    private final String serverKeyStorePassword;

    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path serverTrustStore;

    @XmlElement
    private final String serverTrustStorePassword;

    @XmlElement
    private final SslTrustMode serverTrustMode;

    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path clientKeyStore;

    @XmlElement
    private final String clientKeyStorePassword;

    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path clientTrustStore;

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
            Path knownServersFile) {
        this.tls = tls;
        this.generateKeyStoreIfNotExisted = generateKeyStoreIfNotExisted;
        this.serverKeyStore = serverKeyStore;
        this.serverKeyStorePassword = serverKeyStorePassword;
        this.serverTrustStore = serverTrustStore;
        this.serverTrustStorePassword = serverTrustStorePassword;
        this.serverTrustMode = serverTrustMode;
        this.clientKeyStore = clientKeyStore;
        this.clientKeyStorePassword = clientKeyStorePassword;
        this.clientTrustStore = clientTrustStore;
        this.clientTrustStorePassword = clientTrustStorePassword;
        this.clientTrustMode = clientTrustMode;
        this.knownClientsFile = knownClientsFile;
        this.knownServersFile = knownServersFile;
    }

    private SslConfig() {
        this(null, false, null, null, null, null, null, null, null, null, null, null, null, null);
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

}
