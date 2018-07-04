package com.github.nexus.config;

import com.github.nexus.config.adapters.PathAdapter;
import com.github.nexus.config.adapters.PrivateKeyTypeAdapter;
import com.github.nexus.config.constraints.KeyGen;
import com.github.nexus.config.constraints.ValidPath;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.nio.file.Path;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class PrivateKey {

    @NotNull
    @XmlElement(name = "data")
    private final PrivateKeyData privateKeyData;

    @NotNull
    @XmlAttribute
    @XmlJavaTypeAdapter(PrivateKeyTypeAdapter.class)
    private final PrivateKeyType type;

    //FIXME: Mutable
    @NotNull(groups = KeyGen.class)
    @ValidPath(groups = KeyGen.class)
    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private Path path;

    public PrivateKey(PrivateKeyData privateKeyData, PrivateKeyType type) {
        this.privateKeyData = privateKeyData;
        this.type = type;
    }

    private static PrivateKey create() {
        return new PrivateKey(null, null);
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

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

}
