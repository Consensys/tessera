package com.quorum.tessera.config.keys;

import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.KeyData;

public interface KeyGenerator {

    KeyData generate(String filename, ArgonOptions encryptionOptions);

}
