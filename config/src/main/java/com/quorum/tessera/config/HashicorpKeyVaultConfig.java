package com.quorum.tessera.config;

import com.quorum.tessera.config.adapters.PathAdapter;
import com.quorum.tessera.config.constraints.ValidPath;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.nio.file.Path;

public class HashicorpKeyVaultConfig extends ConfigItem implements KeyVaultConfig {

    @Valid
    @NotNull
    @XmlAttribute
    private String url;

    @Valid
    @XmlElement
    private String approlePath;

    @Valid
    @ValidPath(checkExists = true, message = "File does not exist")
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private Path tlsCertificatePath;

    @Valid
    @ValidPath(checkExists = true, message = "File does not exist")
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private Path tlsKeyPath;

    @Valid
    @ValidPath(checkExists = true, message = "File does not exist")
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private Path tlsServerCertificatePath;

    public HashicorpKeyVaultConfig(String url, String approlePath, Path tlsCertificatePath, Path tlsKeyPath, Path tlsServerCertificatePath) {
        this.url = url;
        this.approlePath = approlePath;
        this.tlsCertificatePath = tlsCertificatePath;
        this.tlsKeyPath = tlsKeyPath;
        this.tlsServerCertificatePath = tlsServerCertificatePath;
    }

    public HashicorpKeyVaultConfig() {
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Path getTlsCertificatePath() {
        return tlsCertificatePath;
    }

    public void setTlsCertificatePath(Path tlsCertificatePath) {
        this.tlsCertificatePath = tlsCertificatePath;
    }

    public Path getTlsKeyPath() {
        return tlsKeyPath;
    }

    public void setTlsKeyPath(Path tlsKeyPath) {
        this.tlsKeyPath = tlsKeyPath;
    }

    public Path getTlsServerCertificatePath() {
        return tlsServerCertificatePath;
    }

    public void setTlsServerCertificatePath(Path tlsServerCertificatePath) {
        this.tlsServerCertificatePath = tlsServerCertificatePath;
    }

    public String getApprolePath() {
        if(approlePath == null) {
            return "approle";
        }
        return approlePath;
    }

    public void setApprolePath(String approlePath) {
        this.approlePath = approlePath;
    }

    @Override
    public KeyVaultType getKeyVaultType() {
        return KeyVaultType.HASHICORP;
    }
}
