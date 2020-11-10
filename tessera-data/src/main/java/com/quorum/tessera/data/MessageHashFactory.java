package com.quorum.tessera.data;

import org.bouncycastle.jcajce.provider.digest.SHA3;

import java.util.ServiceLoader;

public interface MessageHashFactory {

    default MessageHash createFromCipherText(byte[] cipherText) {
        final SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest512();
        final byte[] digest = digestSHA3.digest(cipherText);
        return new MessageHash(digest);
    }

    static MessageHashFactory create() {
        return ServiceLoader.load(MessageHashFactory.class).findFirst().orElse(new MessageHashFactory() {});
    }
}
