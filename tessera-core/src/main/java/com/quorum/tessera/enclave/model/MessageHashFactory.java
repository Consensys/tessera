package com.quorum.tessera.enclave.model;

import com.quorum.tessera.ServiceLoaderUtil;
import org.bouncycastle.jcajce.provider.digest.SHA3;

public interface MessageHashFactory {

    default MessageHash createFromCipherText(byte[] cipherText) {
        final SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest512();
        final byte[] digest = digestSHA3.digest(cipherText);
        return new MessageHash(digest);
    }

    static MessageHashFactory create() {
        return ServiceLoaderUtil.load(MessageHashFactory.class).orElse(new MessageHashFactory() {});
    }
}
