package com.quorum.tessera.config.adapters;

import com.quorum.tessera.config.*;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.quorum.tessera.config.PrivateKeyType.LOCKED;
import static com.quorum.tessera.config.PrivateKeyType.UNLOCKED;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class KeyConfigurationAdapterTest {

    private final KeyConfigurationAdapter keyConfigurationAdapter = new KeyConfigurationAdapter();

    @Test
    public void marshallingNullsPasswords() {

        final KeyConfiguration keyConfiguration = new KeyConfiguration(
            null,
            null,
            singletonList(
                new KeyData(
                    new KeyDataConfig(
                        new PrivateKeyData(null, null, null, null, new ArgonOptions("", 1, 1, 1), "PASSWORD"),
                        LOCKED
                    ), null, null, null, null
                )
            )
        );

        final KeyConfiguration marshalled = keyConfigurationAdapter.marshal(keyConfiguration);

        assertThat(marshalled.getKeyData().get(0).getConfig().getPassword()).isNull();

    }

    @Test
    public void givingBothPasswordTypesThrowsError() {

        final KeyConfiguration keyConfiguration = new KeyConfiguration(Paths.get("testfile"), emptyList(), null);

        final Throwable throwable = catchThrowable(() -> this.keyConfigurationAdapter.unmarshal(keyConfiguration));

        assertThat(throwable)
            .isInstanceOf(ConfigException.class)
            .hasCauseExactlyInstanceOf(RuntimeException.class);

        assertThat(throwable.getCause()).hasMessage("Must specify passwords in file or in config, not both");
    }

    @Test
    public void emptyPasswordsReturnsSameKeys() {

        final KeyData keyData = mock(KeyData.class);
        final KeyDataConfig pkd = mock(KeyDataConfig.class);
        doReturn(UNLOCKED).when(pkd).getType();
        doReturn(pkd).when(keyData).getConfig();

        final KeyConfiguration keyConfiguration = new KeyConfiguration(null, emptyList(), singletonList(keyData));
        final KeyConfiguration unmarshalled = this.keyConfigurationAdapter.unmarshal(keyConfiguration);

        assertThat(unmarshalled.getKeyData()).hasSize(1);
        final KeyData kd = unmarshalled.getKeyData().get(0);

        //unlocked was the only property set pre-unmarshalling
        assertThat(kd.getConfig().getType()).isEqualTo(UNLOCKED);
    }

    @Test
    public void noPasswordsReturnsSameKeys() {

        final KeyData keyData = mock(KeyData.class);
        final KeyDataConfig pkd = mock(KeyDataConfig.class);
        doReturn(UNLOCKED).when(pkd).getType();
        doReturn(pkd).when(keyData).getConfig();

        final KeyConfiguration keyConfiguration = new KeyConfiguration(null, null, singletonList(keyData));
        final KeyConfiguration unmarshalled = this.keyConfigurationAdapter.unmarshal(keyConfiguration);

        assertThat(unmarshalled.getKeyData()).hasSize(1);
        final KeyData kd = unmarshalled.getKeyData().get(0);

        //unlocked was the only property set pre-unmarshalling
        assertThat(kd.getConfig().getType()).isEqualTo(UNLOCKED);
    }

    @Test
    public void passwordsAssignedToKeys() {

        final KeyData keyData = new KeyData(
            new KeyDataConfig(
                new PrivateKeyData(
                    "",
                    "x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC",
                    "7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=",
                    "d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc",
                    new ArgonOptions("id", 10, 1048576, 4),
                    null
                ), LOCKED
            ),
            null, null, null, null
        );


        final KeyConfiguration keyConfiguration = new KeyConfiguration(null, singletonList("q"), singletonList(keyData));
        final KeyConfiguration unmarshalled = this.keyConfigurationAdapter.unmarshal(keyConfiguration);

        assertThat(unmarshalled.getKeyData()).hasSize(1);
        final KeyData kd = unmarshalled.getKeyData().get(0);

        //unlocked was the only property set pre-unmarshalling
        assertThat(kd.getConfig().getPassword()).isEqualTo("q");
    }

    @Test
    public void filePasswordsReadCorrectly() throws IOException {

        final Path passes = Files.createTempFile("passes", ".txt");
        Files.write(passes, "q".getBytes());

        final KeyData keyData = new KeyData(
            new KeyDataConfig(
                new PrivateKeyData(
                    "",
                    "x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC",
                    "7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=",
                    "d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc",
                    new ArgonOptions("id", 10, 1048576, 4),
                    null
                ), LOCKED
            ),
            null, null, null, null
        );


        final KeyConfiguration keyConfiguration = new KeyConfiguration(passes, null, singletonList(keyData));
        final KeyConfiguration unmarshalled = this.keyConfigurationAdapter.unmarshal(keyConfiguration);

        assertThat(unmarshalled.getKeyData()).hasSize(1);
        final KeyData kd = unmarshalled.getKeyData().get(0);

        //unlocked was the only property set pre-unmarshalling
        assertThat(kd.getConfig().getPassword()).isEqualTo("q");

    }

    @Test
    public void fileKeysReadCorrectly() throws IOException {

        final Path pubKey = Files.createTempFile("pub", ".pub");
        final Path privKey = Files.createTempFile("priv", ".key");
        Files.write(privKey, "{\"data\":{\"bytes\":\"nDFwJNHSiT1gNzKBy9WJvMhmYRkW3TzFUmPsNzR6oFk=\"},\"type\":\"unlocked\"}".getBytes());
        Files.write(pubKey, "QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc=".getBytes());

        final KeyData keyData = new KeyData(null, null, null, privKey, pubKey);

        final KeyConfiguration keyConfiguration = new KeyConfiguration(null, singletonList("a"), singletonList(keyData));
        final KeyConfiguration unmarshalled = this.keyConfigurationAdapter.unmarshal(keyConfiguration);

        assertThat(unmarshalled.getKeyData()).hasSize(1);

        assertThat(unmarshalled.getKeyData().get(0).getPublicKey()).isEqualTo("QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc=");
        assertThat(unmarshalled.getKeyData().get(0).getPrivateKey()).isEqualTo("nDFwJNHSiT1gNzKBy9WJvMhmYRkW3TzFUmPsNzR6oFk=");
    }

}
