package com.quorum.tessera.argon2;

import org.junit.Before;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.Arrays;

import static com.quorum.tessera.argon2.Argon2.SALT_LENGTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class Argon2Test {

    private static final ArgonOptions TEST_OPTIONS = new ArgonOptions("i", 1, 1024, 1);

    private byte[] randomSalt;

    private Argon2 argon2 = Argon2.create();

    @Before
    public void init() {
        randomSalt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(randomSalt);
    }

    @Test
    public void hashCalledWithDefaultOptionsWhenOnlyPasswordProvided() {
        final ArgonResult hash = argon2.hash("password", randomSalt);

        assertThat(hash.getOptions()).isEqualToComparingFieldByField(new ArgonOptions("i", 10, 1048576, 4));
    }

    @Test
    public void hashCalledWithCustomOptions() {
        final ArgonOptions options = new ArgonOptions("id", 1, 1024, 1);

        final ArgonResult hash = argon2.hash(options, "password", randomSalt);

        assertThat(hash.getOptions().getAlgorithm()).isEqualTo("id");
        assertThat(hash.getOptions().getIterations()).isEqualTo(1);
        assertThat(hash.getOptions().getParallelism()).isEqualTo(1);
        assertThat(hash.getOptions().getMemory()).isEqualTo(1024);
    }

    @Test
    public void saltGivenIsSameAsSaltReturned() {
        final ArgonResult hash = argon2.hash(TEST_OPTIONS, "password", randomSalt);

        assertThat(hash.getSalt()).isEqualTo(randomSalt);
    }

    @Test
    public void invalidAlgorithmThrowsException() {
        final ArgonOptions invalidOptions = new ArgonOptions("invalid", 1, 1024, 1);
        final Throwable throwable = catchThrowable(() -> this.argon2.hash(invalidOptions, "password", randomSalt));

        assertThat(throwable)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid Argon2 algorithm invalid");
    }

    @Test
    public void differentSaltsProduceDifferentOutputs() {
        final byte[] saltTwo = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(saltTwo);

        final ArgonResult hashOne = argon2.hash(TEST_OPTIONS, "password", randomSalt);
        final ArgonResult hashTwo = argon2.hash(TEST_OPTIONS, "password", saltTwo);

        assertThat(hashOne.getHash()).isNotEqualTo(hashTwo.getHash());
    }

    @Test
    public void sameSaltProduceSameOutput() {
        final byte[] saltTwo = Arrays.copyOf(randomSalt, randomSalt.length);

        final ArgonResult hashOne = argon2.hash(TEST_OPTIONS, "password", randomSalt);
        final ArgonResult hashTwo = argon2.hash(TEST_OPTIONS, "password", saltTwo);

        assertThat(hashOne.getHash()).isEqualTo(hashTwo.getHash());
    }

    @Test
    public void argon2dAlgorithmSelectedIfSetInOptions() {
        final ArgonOptions options = new ArgonOptions("d", 10, 1024, 4);

        final ArgonResult hashOne = argon2.hash(options, "password", randomSalt);

        assertThat(hashOne.getOptions().getAlgorithm()).isEqualTo(options.getAlgorithm());
    }

    @Test
    public void nullPasswordCanBeHashed() {
        final ArgonResult hashOne = argon2.hash(null, randomSalt);

        assertThat(hashOne.getHash()).isNotEmpty();
    }

}
