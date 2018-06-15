package com.github.nexus.key;

import com.github.nexus.nacl.Key;

import javax.json.JsonObject;

public interface KeyEncryptor {

    int SALTLENGTH = 16;

    JsonObject encryptPrivateKey(Key privateKey, String password);

    Key decryptPrivateKey(JsonObject encryptedKey, String password);

}
