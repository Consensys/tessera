package com.quorum.tessera.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class PrivateKeyData extends ConfigItem {

    @XmlElement(name = "bytes")
    private final String value;

    @XmlElement
    private final String snonce;

    @XmlElement
    private final String asalt;

    @XmlElement
    private final String sbox;

    @XmlElement(name = "aopts")
    private final ArgonOptions argonOptions;

    @XmlElement
    private final String password;

    public PrivateKeyData(String value, String snonce, String asalt, String sbox, ArgonOptions argonOptions, String password) {
        this.value = value;
        this.snonce = snonce;
        this.asalt = asalt;
        this.sbox = sbox;
        this.argonOptions = argonOptions;
        this.password = password;
    }

    private static PrivateKeyData create() {
        return new PrivateKeyData(null, null, null, null, null, null);
    }

    public String getValue() {
        return value;
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

    public String getPassword() {
        return password;
    }

}
