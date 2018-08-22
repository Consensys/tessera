package com.quorum.tessera.argon2;

import de.mkammerer.argon2.Argon2Constants;
import org.junit.Before;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class Argon2Test {

    private static final ArgonOptions TEST_OPTIONS = new ArgonOptions("i", 1, 1024, 1);

    private SecureRandom secureRandom = new SecureRandom();

    private Argon2 argon2;

    @Before
    public void init() {
        this.argon2 = Argon2.create();
    }

    @Test
    public void hashCalledWithDefaultOptionsWhenOnlyPasswordProvided() {
        final byte[] salt = new byte[Argon2Constants.DEFAULT_SALT_LENGTH];
        secureRandom.nextBytes(salt);

        final ArgonResult hash = argon2.hash("password", salt);

        assertThat(hash.getOptions()).isEqualToComparingFieldByField(new ArgonOptions("i", 10, 1048576, 4));
    }

    @Test
    public void hashCalledWithCustomOptions() {
        final byte[] salt = new byte[Argon2Constants.DEFAULT_SALT_LENGTH];
        secureRandom.nextBytes(salt);

        final ArgonOptions options = new ArgonOptions("id", 1, 1024, 1);

        final ArgonResult hash = argon2.hash(options, "password", salt);

        assertThat(hash.getOptions().getAlgorithm()).isEqualTo("id");
        assertThat(hash.getOptions().getIterations()).isEqualTo(1);
        assertThat(hash.getOptions().getParallelism()).isEqualTo(1);
        assertThat(hash.getOptions().getMemory()).isEqualTo(1024);
    }

    @Test
    public void saltGivenIsSameAsSaltReturned() {
        final byte[] salt = new byte[Argon2Constants.DEFAULT_SALT_LENGTH];
        secureRandom.nextBytes(salt);

        final ArgonResult hash = argon2.hash(TEST_OPTIONS, "password", salt);

        assertThat(hash.getSalt()).isEqualTo(salt);
    }

    @Test
    public void invalidAlgorithmDefaultsToi() {
        final byte[] salt = new byte[Argon2Constants.DEFAULT_SALT_LENGTH];
        secureRandom.nextBytes(salt);

        final ArgonResult hash = argon2.hash(TEST_OPTIONS, "password", salt);

        assertThat(hash.getOptions().getAlgorithm()).isEqualTo("i");
        assertThat(hash.getOptions().getIterations()).isEqualTo(1);
        assertThat(hash.getOptions().getParallelism()).isEqualTo(1);
        assertThat(hash.getOptions().getMemory()).isEqualTo(1024);
    }

    @Test
    public void differentSaltsProduceDifferentOutputs() {

        final byte[] saltOne = new byte[Argon2Constants.DEFAULT_SALT_LENGTH];
        final byte[] saltTwo = new byte[Argon2Constants.DEFAULT_SALT_LENGTH];
        secureRandom.nextBytes(saltOne);
        secureRandom.nextBytes(saltTwo);

        final ArgonResult hashOne = argon2.hash(TEST_OPTIONS, "password", saltOne);
        final ArgonResult hashTwo = argon2.hash(TEST_OPTIONS, "password", saltTwo);

        assertThat(hashOne.getHash()).isNotEqualTo(hashTwo.getHash());

    }

    @Test
    public void sameSaltProduceSameOutput() {

        final byte[] saltOne = new byte[Argon2Constants.DEFAULT_SALT_LENGTH];
        secureRandom.nextBytes(saltOne);
        final byte[] saltTwo = Arrays.copyOf(saltOne, saltOne.length);

        final ArgonResult hashOne = argon2.hash(TEST_OPTIONS, "password", saltOne);
        final ArgonResult hashTwo = argon2.hash(TEST_OPTIONS, "password", saltTwo);

        assertThat(hashOne.getHash()).isEqualTo(hashTwo.getHash());

    }

    @Test
    public void argon2dAlgorithmSelectedIfSetInOptions() {

        final byte[] saltOne = new byte[Argon2Constants.DEFAULT_SALT_LENGTH];
        secureRandom.nextBytes(saltOne);

        final ArgonOptions options = new ArgonOptions("d", 10, 1024, 4);

        final ArgonResult hashOne = argon2.hash(options, "password", saltOne);

        assertThat(hashOne.getOptions().getAlgorithm()).isEqualTo(options.getAlgorithm());

    }

}
