package com.quorum.tessera.data;

import org.bouncycastle.jcajce.provider.digest.SHA3;

public class MessageHashFactoryImpl implements MessageHashFactory {
    @Override
    public MessageHash createFromCipherText(byte[] cipherText) {
        final SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest512();
        final byte[] digest = digestSHA3.digest(cipherText);
        return new MessageHash(digest);
    }
}
