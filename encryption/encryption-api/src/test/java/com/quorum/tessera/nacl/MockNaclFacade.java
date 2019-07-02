package com.quorum.tessera.nacl;

import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.encryption.MasterKey;
import com.quorum.tessera.encryption.PrivateKey;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.encryption.SharedKey;

public enum MockNaclFacade implements NaclFacade {
    INSTANCE;

    @Override
    public Nonce randomNonce() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public KeyPair generateNewKeys() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SharedKey createSingleKey() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SharedKey computeSharedKey(PublicKey publicKey, PrivateKey privateKey) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] seal(byte[] message, Nonce nonce, PublicKey publicKey, PrivateKey privateKey) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] open(byte[] cipherText, Nonce nonce, PublicKey publicKey, PrivateKey privateKey) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] sealAfterPrecomputation(byte[] message, Nonce nonce, SharedKey sharedKey) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] openAfterPrecomputation(byte[] cipherText, Nonce nonce, SharedKey sharedKey) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MasterKey createMasterKey() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] sealAfterPrecomputation(byte[] message, Nonce nonce, MasterKey masterKey) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
