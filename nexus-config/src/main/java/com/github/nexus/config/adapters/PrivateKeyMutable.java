package com.github.nexus.config.adapters;

import com.github.nexus.config.PrivateKey;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.nio.file.Path;

/**
 * The POJO class that JAXB deserialises to
 */
public class PrivateKeyMutable {

    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path legacyPath;

    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path path;

    @XmlElement
    private final String value;

    @XmlElement(required = true)
    private final String password;

    @XmlElement
    private final PrivateKey contents;

    public PrivateKeyMutable() {
        this(null, null, null, null, null);
    }

    public PrivateKeyMutable(final Path legacyPath,
                             final Path path,
                             final String value,
                             final String password,
                             final PrivateKey contents) {
        this.legacyPath = legacyPath;
        this.path = path;
        this.value = value;
        this.password = password;
        this.contents = contents;
    }

    public Path getLegacyPath() {
        return legacyPath;
    }

    public Path getPath() {
        return path;
    }

    public String getValue() {
        return value;
    }

    public String getPassword() {
        return password;
    }

    public PrivateKey getPrivateKey() {
        return contents;
    }

}
