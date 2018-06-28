package com.github.nexus.config;

import javax.xml.bind.annotation.XmlElement;

public class PrivateKey {

    @XmlElement(name = "bytes")
    private final String value;

    private final String password;

    private final PrivateKeyType type;

    private final String snonce;

    private final String asalt;

    private final String sbox;

    private final ArgonOptions argonOptions;

    public PrivateKey() {
        this(null, null, null, null, null, null, null);
    }

    public PrivateKey(final String value,
                      final String password,
                      final PrivateKeyType type,
                      final String snonce,
                      final String asalt,
                      final String sbox,
                      final ArgonOptions argonOptions) {
        this.value = value;
        this.password = password;
        this.type = type;
        this.snonce = snonce;
        this.asalt = asalt;
        this.sbox = sbox;
        this.argonOptions = argonOptions;
    }

    public String getValue() {
        return this.value;
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
