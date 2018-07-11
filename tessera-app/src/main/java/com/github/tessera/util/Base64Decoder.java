package com.github.tessera.util;

import com.github.tessera.util.exception.DecodingException;
import java.util.Base64;

public interface Base64Decoder {

    default byte[] decode(String data) {
        try {
            return Base64.getDecoder().decode(data);
        } catch (IllegalArgumentException ex) {
            throw new DecodingException(ex);
        }
    }

    default String encodeToString(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }
    

    static Base64Decoder create() {
        return new Base64Decoder() {};
    }
    

}
