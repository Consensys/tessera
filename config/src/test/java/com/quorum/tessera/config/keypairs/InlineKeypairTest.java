package com.quorum.tessera.config.keypairs;

import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.KeyDataConfig;
import com.quorum.tessera.config.PrivateKeyData;
import com.quorum.tessera.config.PrivateKeyType;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InlineKeypairTest {

    @Test
    public void unlockedKeyGetsValue() {
        final KeyDataConfig privKeyDataConfig = new KeyDataConfig(
            new PrivateKeyData("Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=", null, null, null, null),
            PrivateKeyType.UNLOCKED
        );

        final InlineKeypair result = new InlineKeypair("public", privKeyDataConfig);

        assertThat(result.getPrivateKey()).isEqualTo("Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=");
    }

    @Test
    public void nullPasswordGivesNullKey() {
        final KeyDataConfig privKeyDataConfig = new KeyDataConfig(
            new PrivateKeyData(
                "Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=",
                "yb7M8aRJzgxoJM2NecAPcmSVWDW1tRjv",
                "MIqkFlgR2BWEpx2U0rObGg==",
                "Gtvp1t6XZEiFVyaE/LHiP1+yvOIBBoiOL+bKeqcKgpiNt4j1oDDoqCC47UJpmQRC",
                new ArgonOptions("i", 10, 1048576, 4)
            ),
            PrivateKeyType.LOCKED
        );

        final InlineKeypair result = new InlineKeypair("public", privKeyDataConfig);
        result.withPassword(null);

        assertThat(result.getPrivateKey()).isNull();
    }

    @Test
    public void updatingPasswordsAttemptsToDecryptAgain() {
        final KeyDataConfig privKeyDataConfig = new KeyDataConfig(
            new PrivateKeyData(
                "Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=",
                "yb7M8aRJzgxoJM2NecAPcmSVWDW1tRjv",
                "MIqkFlgR2BWEpx2U0rObGg==",
                "Gtvp1t6XZEiFVyaE/LHiP1+yvOIBBoiOL+bKeqcKgpiNt4j1oDDoqCC47UJpmQRC",
                new ArgonOptions("i", 10, 1048576, 4)
            ),
            PrivateKeyType.LOCKED
        );

        final InlineKeypair result = new InlineKeypair("public", privKeyDataConfig);
        result.withPassword("wrong-password");

        assertThat(result.getPrivateKey()).isEqualTo("NACL_FAILURE");

        //change password and attempt again
        result.withPassword("a");

        assertThat(result.getPrivateKey()).isEqualTo("w+itzh2vfuGjiGYEVJtqpiJVUmI5vGUK4CzMErxa+GY=");
    }

    @Test
    public void incorrectPasswordGetsCorrectFailureToken() {
        final KeyDataConfig privKeyDataConfig = new KeyDataConfig(
            new PrivateKeyData(
                "Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=",
                "yb7M8aRJzgxoJM2NecAPcmSVWDW1tRjv",
                "MIqkFlgR2BWEpx2U0rObGg==",
                "Gtvp1t6XZEiFVyaE/LHiP1+yvOIBBoiOL+bKeqcKgpiNt4j1oDDoqCC47UJpmQRC",
                new ArgonOptions("i", 10, 1048576, 4)
            ),
            PrivateKeyType.LOCKED
        );

        final InlineKeypair result = new InlineKeypair("public", privKeyDataConfig);
        result.withPassword("invalid-password");

        assertThat(result.getPrivateKey()).isEqualTo("NACL_FAILURE");
    }

    @Test
    public void correctPasswordGetsCorrectKey() {
        final KeyDataConfig privKeyDataConfig = new KeyDataConfig(
            new PrivateKeyData(
                "Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=",
                "yb7M8aRJzgxoJM2NecAPcmSVWDW1tRjv",
                "MIqkFlgR2BWEpx2U0rObGg==",
                "Gtvp1t6XZEiFVyaE/LHiP1+yvOIBBoiOL+bKeqcKgpiNt4j1oDDoqCC47UJpmQRC",
                new ArgonOptions("i", 10, 1048576, 4)
            ),
            PrivateKeyType.LOCKED
        );

        final InlineKeypair result = new InlineKeypair("public", privKeyDataConfig);
        result.withPassword("a");

        assertThat(result.getPrivateKey()).isEqualTo("w+itzh2vfuGjiGYEVJtqpiJVUmI5vGUK4CzMErxa+GY=");
    }

}
