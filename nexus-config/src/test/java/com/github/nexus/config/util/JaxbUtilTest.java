package com.github.nexus.config.util;

import com.github.nexus.config.KeyDataConfig;
import com.github.nexus.config.PrivateKeyType;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JaxbUtilTest {

    @Test
    public void unmarshalLocked() {

        KeyDataConfig result = JaxbUtil.unmarshal(getClass().getResourceAsStream("/lockedprivatekey.json"), KeyDataConfig.class);
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(PrivateKeyType.LOCKED);
        assertThat(result.getPrivateKeyData()).isNotNull();

        assertThat(result.getSnonce()).isEqualTo("x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC");
        assertThat(result.getAsalt()).isEqualTo("7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=");
        assertThat(result.getSbox()).isEqualTo("d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc");

        assertThat(result.getArgonOptions()).isNotNull();
        assertThat(result.getArgonOptions().getAlgorithm()).isEqualTo("id");
        assertThat(result.getArgonOptions().getIterations()).isEqualTo(10);
        assertThat(result.getArgonOptions().getParallelism()).isEqualTo(4);
        assertThat(result.getArgonOptions().getMemory()).isEqualTo(1048576);
    }

}
