package com.quorum.tessera.key.vault.hashicorp;

import com.bettercloud.vault.SslConfig;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SslConfigFactoryTest {

    @Test
    public void create() {
        SslConfigFactory sslConfigFactory = new SslConfigFactory();
        SslConfig emptySslConfig = new SslConfig();

        SslConfig result = sslConfigFactory.create();

        assertThat(result).isEqualToComparingFieldByField(emptySslConfig);
    }
}
