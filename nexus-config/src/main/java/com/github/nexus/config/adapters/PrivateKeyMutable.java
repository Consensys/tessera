package com.github.nexus.config.adapters;

import com.github.nexus.config.ArgonOptions;
import com.github.nexus.config.PrivateKeyType;

import javax.xml.bind.annotation.XmlAttribute;
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

    @XmlAttribute
    private final PrivateKeyType type;

    @XmlElement
    private final String snonce;

    @XmlElement
    private final String asalt;

    @XmlElement
    private final String sbox;

    @XmlElement
    private final ArgonOptions argonOptions;

    public PrivateKeyMutable() {
        this(null, null, null, null, null, null, null, null, null);
    }

    public PrivateKeyMutable(final Path legacyPath,
                             final Path path,
                             final String value,
                             final String password,
                             final PrivateKeyType type,
                             final String snonce,
                             final String asalt,
                             final String sbox,
                             final ArgonOptions argonOptions) {
        this.legacyPath = legacyPath;
        this.path = path;
        this.value = value;
        this.password = password;
        this.type = type;
        this.snonce = snonce;
        this.asalt = asalt;
        this.sbox = sbox;
        this.argonOptions = argonOptions;
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

    public PrivateKeyType getType() {
        return type;
    }

    public String getSnonce() {
        return snonce;
    }

    public String getAsalt() {
        return asalt;
    }

    public String getSbox() {
        return sbox;
    }

    public ArgonOptions getArgonOptions() {
        return argonOptions;
    }

}
