package com.quorum.tessera.encryption;

class PublicKeyImpl extends BaseKey implements PublicKey {

    PublicKeyImpl(byte[] keyBytes) {
        super(keyBytes);
    }
}
