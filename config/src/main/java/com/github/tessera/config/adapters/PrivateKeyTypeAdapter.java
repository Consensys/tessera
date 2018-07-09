package com.github.tessera.config.adapters;

import com.github.tessera.config.PrivateKeyType;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashMap;
import java.util.Map;

public class PrivateKeyTypeAdapter extends XmlAdapter<String, PrivateKeyType> {

    private static final Map<String, PrivateKeyType> MAPPING = new HashMap<String, PrivateKeyType>() {
        {
            put("unlocked", PrivateKeyType.UNLOCKED);
            put("argon2sbox", PrivateKeyType.LOCKED);

        }
    };

    @Override
    public PrivateKeyType unmarshal(String v) {
        return MAPPING.get(v);
    }

    @Override
    public String marshal(PrivateKeyType v) {
        return MAPPING.entrySet().stream()
                .filter(e -> e.getValue() == v).map(e -> e.getKey())
                .findAny()
                .orElse(null);
    }

}
