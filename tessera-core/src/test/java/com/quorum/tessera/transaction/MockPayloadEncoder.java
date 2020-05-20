package com.quorum.tessera.transaction;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;

import static org.mockito.Mockito.mock;

public class MockPayloadEncoder implements PayloadEncoder {

    @Override
    public byte[] encode(EncodedPayload payload) {
        return new byte[0];
    }

    @Override
    public EncodedPayload decode(byte[] input) {
        return mock(EncodedPayload.class);
    }

    @Override
    public EncodedPayload forRecipient(EncodedPayload input, PublicKey recipient) {
        return mock(EncodedPayload.class);
    }

    @Override
    public EncodedPayload withRecipient(EncodedPayload input, PublicKey recipient) {
        return mock(EncodedPayload.class);
    }
}
