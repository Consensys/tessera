package com.quorum.tessera.config.keys;

import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.KeyVaultConfig;

public interface KeyGenerator {

    KeyData generate(String filename, ArgonOptions encryptionOptions, KeyVaultConfig keyVaultConfig);

}
