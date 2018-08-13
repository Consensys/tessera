package com.quorum.tessera.config;

import com.quorum.tessera.config.adapters.PrivateKeyTypeAdapter;
import java.util.Optional;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(factoryMethod = "create")
public class KeyDataConfig {

    @NotNull
    @XmlElement(name = "data")
    private final PrivateKeyData privateKeyData;

    @NotNull
    @XmlAttribute
    @XmlJavaTypeAdapter(PrivateKeyTypeAdapter.class)
    private final PrivateKeyType type;

    public KeyDataConfig(PrivateKeyData privateKeyData, PrivateKeyType type) {
        this.privateKeyData = privateKeyData;
        this.type = type;
    }

    private static KeyDataConfig create() {
        return new KeyDataConfig(null, null);
    }

    public PrivateKeyType getType() {
        return type;
    }

    public PrivateKeyData getPrivateKeyData() {
        return privateKeyData;
    }

    public String getValue() {
        return Optional.ofNullable(privateKeyData)
                .map(PrivateKeyData::getValue)
                .orElse(null);  
    }

    public String getSnonce() {
        return Optional.ofNullable(privateKeyData)
                .map(PrivateKeyData::getSnonce)
                .orElse(null);   
    }

    public String getAsalt() {
        return Optional.ofNullable(privateKeyData)
                .map(PrivateKeyData::getAsalt)
                .orElse(null);   
    }

    public String getSbox() {
        return Optional.ofNullable(privateKeyData)
                .map(PrivateKeyData::getSbox)
                .orElse(null);   
    }

    public ArgonOptions getArgonOptions() {
        return Optional.ofNullable(privateKeyData)
                .map(PrivateKeyData::getArgonOptions)
                .orElse(null);   
    }

    public String getPassword() {
        return Optional.ofNullable(privateKeyData)
                .map(PrivateKeyData::getPassword)
                .orElse(null);
    }

}
