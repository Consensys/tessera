package com.quorum.tessera.transaction;

import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.enclave.model.MessageHashFactory;

public class MockMessageHashFactory implements MessageHashFactory {

    @Override
    public MessageHash createFromCipherText(byte[] cipherText) {
        return new MessageHash(cipherText);
    }
}
