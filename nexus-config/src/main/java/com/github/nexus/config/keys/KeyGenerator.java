package com.github.nexus.config.keys;

import com.github.nexus.config.KeyData;
import com.github.nexus.config.KeyDataConfig;

public interface KeyGenerator {

    KeyData generate(KeyDataConfig keyData);

}
