package com.github.tessera.config.keys;

import com.github.tessera.config.KeyData;
import com.github.tessera.config.KeyDataConfig;

public interface KeyGenerator {

    KeyData generate(KeyDataConfig keyData);

}
