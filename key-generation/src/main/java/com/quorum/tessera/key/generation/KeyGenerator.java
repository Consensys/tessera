package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.ArgonOptions;

public interface KeyGenerator {

  GeneratedKeyPair generate(
      String filename, ArgonOptions encryptionOptions, KeyVaultOptions keyVaultOptions);
}
