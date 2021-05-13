package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;

public interface KeyGenerator {

  ConfigKeyPair generate(
      String filename, ArgonOptions encryptionOptions, KeyVaultOptions keyVaultOptions);
}
