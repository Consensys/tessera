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
    @ValidPath(checkExists = true, message = "File does not exist")
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private Path tlsCertificatePath;

    public HashicorpKeyVaultConfig(String url, Path tlsCertificatePath) {
        this.url = url;
        this.tlsCertificatePath = tlsCertificatePath;
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

    @Override
    public KeyVaultType getKeyVaultType() {
        return KeyVaultType.HASHICORP;
    }
}
