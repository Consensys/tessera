package com.github.tessera.config;

import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class PrivateKeyData {

    @XmlElement(name = "bytes")
    @XmlSchemaType(name = "anyURI")
    private final String value;

    @XmlElement
    @XmlSchemaType(name = "anyURI")
    private final String snonce;

    @XmlElement
    @XmlSchemaType(name = "anyURI")
    private final String asalt;

    @XmlElement
    @XmlSchemaType(name = "anyURI")
    private final String sbox;

    @XmlElement(name = "aopts")
    private final ArgonOptions argonOptions;

    @XmlElement
    @XmlSchemaType(name = "anyURI")
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

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.value);
        hash = 53 * hash + Objects.hashCode(this.snonce);
        hash = 53 * hash + Objects.hashCode(this.asalt);
        hash = 53 * hash + Objects.hashCode(this.sbox);
        hash = 53 * hash + Objects.hashCode(this.argonOptions);
        hash = 53 * hash + Objects.hashCode(this.password);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PrivateKeyData other = (PrivateKeyData) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        if (!Objects.equals(this.snonce, other.snonce)) {
            return false;
        }
        if (!Objects.equals(this.asalt, other.asalt)) {
            return false;
        }
        if (!Objects.equals(this.sbox, other.sbox)) {
            return false;
        }
        if (!Objects.equals(this.password, other.password)) {
            return false;
        }
        return Objects.equals(this.argonOptions, other.argonOptions);
    }

}
