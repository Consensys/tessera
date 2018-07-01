package com.github.nexus.config;

import com.github.nexus.config.adapters.PathAdapter;
import com.github.nexus.config.adapters.PrivateKeyTypeAdapter;
import com.github.nexus.config.constraints.ValidPath;
import java.nio.file.Path;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class PrivateKey {

    @NotNull
    @XmlElement(name = "data")
    private final PrivateKeyData privateKeyData;

    @ValidPath
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path path;

    @NotNull
    @XmlAttribute
    @XmlJavaTypeAdapter(PrivateKeyTypeAdapter.class)
    private final PrivateKeyType type;

    public PrivateKey(PrivateKeyData privateKeyData, Path path, PrivateKeyType type) {
        this.privateKeyData = privateKeyData;
        this.path = path;
        this.type = type;
    }

    private static PrivateKey create() {
        return new PrivateKey(null, null, null);
    }

    public Path getPath() {
        return path;
    }

    public PrivateKeyType getType() {
        return type;
    }

    public PrivateKeyData getPrivateKeyData() {
        return privateKeyData;
    }

    public String getValue() {
        return privateKeyData.getValue();
    }

    public String getSnonce() {
        return privateKeyData.getSnonce();
    }

    public String getAsalt() {
        return privateKeyData.getAsalt();
    }

    public String getSbox() {
        return privateKeyData.getSbox();
    }

    public ArgonOptions getArgonOptions() {
        return privateKeyData.getArgonOptions();
    }

    public String getPassword() {
        return privateKeyData.getPassword();
    }

}
