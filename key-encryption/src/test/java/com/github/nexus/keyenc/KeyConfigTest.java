package com.github.nexus.keyenc;

import com.github.nexus.argon2.ArgonOptions;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyConfigTest {

    @Test
    public void buildWithoutArgonOptions() {

        final KeyConfig keyConfig = KeyConfig.Builder.create()
            .password("SECRET")
            .value("SOMEVALUE")
            .snonce("SNONCE".getBytes())
            .asalt("uZAfjmMwEepP8kzZCnmH6g==".getBytes())
            .sbox("SBOX".getBytes())
            .argonAlgorithm("id")
            .argonIterations(1)
            .argonMemory(2)
            .argonParallelism(3)
            .build();

        assertThat(keyConfig).isNotNull();
        assertThat(keyConfig.getPassword()).isEqualTo("SECRET");
        assertThat(keyConfig.getValue()).isEqualTo("SOMEVALUE");
        assertThat(keyConfig.getSnonce()).isEqualTo("SNONCE".getBytes());
        assertThat(keyConfig.getSbox()).isEqualTo("SBOX".getBytes());
        assertThat(keyConfig.getArgonOptions().getAlgorithm()).isEqualTo("id");
        assertThat(keyConfig.getArgonOptions().getIterations()).isEqualTo(1);
        assertThat(keyConfig.getArgonOptions().getMemory()).isEqualTo(2);
        assertThat(keyConfig.getArgonOptions().getParallelism()).isEqualTo(3);

    }

    @Test
    public void buildWithArgonOptions() {

        final ArgonOptions argonOptions = new ArgonOptions("id", 1, 2, 3);

        final KeyConfig keyConfig = KeyConfig.Builder.create()
            .password("SECRET")
            .value("SOMEVALUE")
            .snonce("SNONCE".getBytes())
            .asalt("uZAfjmMwEepP8kzZCnmH6g==".getBytes())
            .sbox("SBOX".getBytes())
            .argonOptions(argonOptions)
            .build();

        assertThat(keyConfig).isNotNull();
        assertThat(keyConfig.getPassword()).isEqualTo("SECRET");
        assertThat(keyConfig.getValue()).isEqualTo("SOMEVALUE");
        assertThat(keyConfig.getSnonce()).isEqualTo("SNONCE".getBytes());
        assertThat(keyConfig.getSbox()).isEqualTo("SBOX".getBytes());
        assertThat(keyConfig.getArgonOptions().getAlgorithm()).isEqualTo("id");
        assertThat(keyConfig.getArgonOptions().getIterations()).isEqualTo(1);
        assertThat(keyConfig.getArgonOptions().getMemory()).isEqualTo(2);
        assertThat(keyConfig.getArgonOptions().getParallelism()).isEqualTo(3);
    }

}
