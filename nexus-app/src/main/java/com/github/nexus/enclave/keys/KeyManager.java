package com.github.nexus.enclave.keys;

import com.github.nexus.enclave.keys.model.Key;
import com.github.nexus.enclave.keys.model.KeyPair;

import java.nio.file.Path;

public interface KeyManager {

    Key getPublicKeyForPrivateKey(Key privateKey);

    Key getPrivateKeyForPublicKey(Key publicKey);

    KeyPair generateNewKeys(String name);

    KeyPair loadKeypair(Path publicKeyPath, Path privateKeyPath);

}
