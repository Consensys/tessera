package com.quorum.tessera.nacl;

public enum MockNaclFacade implements NaclFacade {

    INSTANCE;

    @Override
    public Key computeSharedKey(final Key keyOne, final Key keyTwo) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] seal(final byte[] message, Nonce nonce, final Key publicKey, final Key privateKey) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] open(final byte[] cipherText, final Nonce nonce, final Key publicKey, final Key privateKey) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] sealAfterPrecomputation(final byte[] message, final Nonce nonce, final Key sharedKey) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] openAfterPrecomputation(final byte[] input, final Nonce nonce, final Key sharedKey) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Nonce randomNonce() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public KeyPair generateNewKeys() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Key createSingleKey() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
