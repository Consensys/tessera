package com.quorum.tessera.data;

import java.util.ServiceLoader;

public interface MessageHashFactory {

    MessageHash createFromCipherText(byte[] cipherText);

    static MessageHashFactory create() {
        return ServiceLoader.load(MessageHashFactory.class).findFirst().get();
    }
}
