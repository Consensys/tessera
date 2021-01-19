package com.quorum.tessera.config.adapters;

import com.quorum.tessera.config.ClientMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ClientModeAdapter extends XmlAdapter<String, ClientMode> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientModeAdapter.class);

    @Override
    public ClientMode unmarshal(String v) {
        try {
            return ClientMode.valueOf(v.toUpperCase());
        } catch (IllegalArgumentException ex) {
            LOGGER.warn("Client mode value not valid. Using default mode = TESSERA");
            return ClientMode.TESSERA;
        }
    }

    @Override
    public String marshal(ClientMode v) {
        return v.name().toLowerCase();
    }
}
