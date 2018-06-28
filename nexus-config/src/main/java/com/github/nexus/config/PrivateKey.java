package com.github.nexus.config;

import com.github.nexus.config.util.PathUtil;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.nio.file.Path;
import java.util.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class PrivateKey {

    @XmlElement(type = String.class, name = "legacyPath")
    @XmlJavaTypeAdapter(LegacyPrivateKeyFileAdapter.class)
    private final LegacyPrivateKeyFile legacyKey;

    @XmlElement(type = String.class)
    @XmlJavaTypeAdapter(PathAdapter.class)
    private Path path;

    @XmlSchemaType(name = "anyURI")
    private String value;

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

    public PrivateKey(final LegacyPrivateKeyFile legacyKey,
                      Path path,
                      String value,
                      String password,
                      PrivateKeyType type,
                      String snonce,
                      String asalt,
                      String sbox,
                      ArgonOptions argonOptions) {

        this.legacyKey = legacyKey;
        this.path = path;
        this.value = value;
        this.password = password;
        this.type = type;
        this.snonce = snonce;
        this.asalt = asalt;
        this.sbox = sbox;
        this.argonOptions = argonOptions;
    }

    public PrivateKey() {
        this(null, null, null, null, null, null, null, null, null);
    }

    private static PrivateKey create() {
        return new PrivateKey();
    }

    public Path getPath() {
        return this.path;
    }

    public String getValue() {
        if (Objects.nonNull(legacyKey)) {
            return legacyKey.getBytes();
        } else if (Objects.nonNull(path)) {
            return PathUtil.readData(path, value);
        }

        return this.value;
    }

    public String getPassword() {
        return password;
    }

    public PrivateKeyType getType() {
        if (Objects.nonNull(legacyKey)) {
            return legacyKey.getType();
        }

        return type;
    }

    public String getSnonce() {
        if (Objects.nonNull(legacyKey)) {
            return legacyKey.getSnonce();
        }

        return snonce;
    }

    public String getAsalt() {
        if (Objects.nonNull(legacyKey)) {
            return legacyKey.getAsalt();
        }

        return asalt;
    }

    public String getSbox() {
        if (Objects.nonNull(legacyKey)) {
            return legacyKey.getSbox();
        }

        return sbox;
    }

    public ArgonOptions getArgonOptions() {
        if (Objects.nonNull(legacyKey)) {
            return legacyKey.getArgonOptions();
        }

        return argonOptions;
    }

    public LegacyPrivateKeyFile getLegacyKey() {
        return legacyKey;
    }
}
