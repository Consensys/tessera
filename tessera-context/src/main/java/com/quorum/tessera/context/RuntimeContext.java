package com.quorum.tessera.context;

import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.encryption.KeyPair;

import java.util.List;

public interface RuntimeContext {

    List<KeyPair> getKeys();

    KeyEncryptor getKeyEncryptor();





}
