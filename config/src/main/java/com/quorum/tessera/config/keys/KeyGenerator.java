package com.quorum.tessera.config.keys;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.KeyDataConfig;

public interface KeyGenerator {

    KeyData generate(KeyDataConfig keyData);

}
