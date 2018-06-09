package com.github.nexus.nacl.jnacl;

public interface SecretBox {

    int cryptoBoxBeforenm(byte[] output, byte[] publicKey, byte[] privateKey);

    int cryptoBoxAfternm(byte[] output, byte[] message, int messageLength, byte[] nonce, byte[] sharedKey);

    int cryptoBoxOpenAfternm(byte[] output, byte[] message, int messageLength, byte[] nonce, byte[] sharedKey);

    int cryptoBoxKeypair(byte[] publicKey, byte[] privateKey);

}
