package com.quorum.tessera.config;

import com.quorum.tessera.config.adapters.PathAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.nio.file.Path;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class KeyConfiguration extends ConfigItem {

    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path passwordFile;

    private final List<String> passwords;

    private final List<KeyData> keyData;

    public KeyConfiguration(final Path passwordFile, final List<String> passwords, final List<KeyData> keyData) {
        this.passwordFile = passwordFile;
        this.passwords = passwords;
        this.keyData = keyData;
    }

    private static KeyConfiguration create() {
        return new KeyConfiguration(null, null, null);
    }

    public Path getPasswordFile() {
        return this.passwordFile;
    }

    public List<String> getPasswords() {
        return this.passwords;
    }

    public List<KeyData> getKeyData() {
        return this.keyData;
    }

}
