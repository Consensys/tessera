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
    private final Path path;

    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private final Path rawPath;

    @XmlElement
    private final String value;

    @XmlElement(required = true)
    private final String password;

    @XmlElement
    private final PrivateKey contents;

    public PrivateKeyMutable() {
        this(null, null, null, null, null);
    }

    public PrivateKeyMutable(final Path path,
                             final Path rawPath,
                             final String value,
                             final String password,
                             final PrivateKey contents) {
        this.path = path;
        this.rawPath = rawPath;
        this.value = value;
        this.password = password;
        this.contents = contents;
    }

    public Path getPath() {
        return path;
    }

    public Path getRawPath() {
        return rawPath;
    }

    public String getValue() {
        return value;
    }

    public String getPassword() {
        return password;
    }

    public PrivateKey getContents() {
        return contents;
    }

}
