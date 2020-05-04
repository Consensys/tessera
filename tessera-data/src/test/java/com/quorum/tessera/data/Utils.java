package com.quorum.tessera.data;

import com.quorum.tessera.data.staging.MessageHashStr;

import java.util.UUID;

public interface Utils {

    static MessageHash createHash() {
        return new MessageHash(randomBytes());
    }

    static MessageHashStr createHashStr() {
        return new MessageHashStr(randomBytes());
    }

    static byte[] randomBytes() {
        return UUID.randomUUID().toString().getBytes();
    }

    static byte[] cipherText() {
        return randomBytes();
    }

    static byte[] cipherTextNonce() {
        return randomBytes();
    }

}
