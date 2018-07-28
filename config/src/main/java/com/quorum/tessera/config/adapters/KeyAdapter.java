package com.quorum.tessera.config.adapters;

import com.quorum.tessera.nacl.Key;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Base64;

public class KeyAdapter extends XmlAdapter<String, Key> {

    @Override
    public Key unmarshal(final String input) {
        return new Key(Base64.getDecoder().decode(input));
    }

    @Override
    public String marshal(final Key input) {
        return input.toString();
    }

}
