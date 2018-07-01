package com.github.nexus.config.adapters;

import com.github.nexus.config.PrivateKeyType;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class PrivateKeyTypeAdapter extends XmlAdapter<String, PrivateKeyType> {

    private static final Map<String, PrivateKeyType> MAPPING = new HashMap<String, PrivateKeyType>() {
        {
            put("unlocked", PrivateKeyType.UNLOCKED);
            put("argon2sbox", PrivateKeyType.LOCKED);

        }
    };

    @Override
    public PrivateKeyType unmarshal(String v) throws Exception {
        return MAPPING.get(v);
    }

    @Override
    public String marshal(PrivateKeyType v) throws Exception {
        return MAPPING.entrySet().stream()
                .filter(e -> e.getValue() == v).map(e -> e.getKey())
                .findAny()
                .orElse(null);
    }

}
