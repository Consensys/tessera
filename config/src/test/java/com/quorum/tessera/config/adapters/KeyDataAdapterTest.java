package com.quorum.tessera.config.adapters;

import com.quorum.tessera.config.*;
import com.quorum.tessera.io.FilesDelegate;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.quorum.tessera.config.PrivateKeyType.UNLOCKED;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KeyDataAdapterTest {

    private KeyDataAdapter adapter = new KeyDataAdapter();

    @Before
    public void onSetup() {
        adapter = new KeyDataAdapter();
    }

    @Test
    public void marshallUnlockedKey() {

        final KeyData keyData = new KeyData(new KeyDataConfig(null, UNLOCKED), "PRIV", "PUB", null, null, null);

        final KeyData marshalledKey = adapter.marshal(keyData);

        assertThat(marshalledKey.getPrivateKey()).isEqualTo("PRIV");
        assertThat(marshalledKey.getPublicKey()).isEqualTo("PUB");
        assertThat(marshalledKey.getConfig()).isEqualToComparingFieldByField(new KeyDataConfig(null, UNLOCKED));

    }

    @Test
    public void marshallKeyWithoutConfiguration() {
        final KeyData keyData = new KeyData(null, "PRIV", "PUB", null, null, null);

        final KeyData marshalledKey = adapter.marshal(keyData);

        assertThat(marshalledKey.getPrivateKey()).isEqualTo("PRIV");
        assertThat(marshalledKey.getPublicKey()).isEqualTo("PUB");
        assertThat(marshalledKey.getConfig()).isNull();
        assertThat(marshalledKey.getPrivateKeyPath()).isNull();
        assertThat(marshalledKey.getPublicKeyPath()).isNull();
    }

    @Test
    public void marshallLockedKeyNullifiesPrivateKey() {

        final KeyData keyData = new KeyData(
                new KeyDataConfig(new PrivateKeyData(null, null, null, null, null, null), PrivateKeyType.LOCKED),
                "PRIV", "PUB", null, null, null
        );

        final KeyData marshalledKey = adapter.marshal(keyData);

        assertThat(marshalledKey.getConfig()).isEqualToComparingFieldByField(keyData.getConfig());
        assertThat(marshalledKey.getPublicKey()).isEqualTo("PUB");
        assertThat(marshalledKey.getPrivateKey()).isNull();

    }

    @Test
    public void marshallKeysWithUnlockedPrivateKey() {

        final KeyData keyData = new KeyData(
                new KeyDataConfig(
                        new PrivateKeyData("LITERAL_PRIVATE", null, null, null, null, null),
                        UNLOCKED
                ),
                null,
                "PUB",
                null,
                null,
                null
        );

        final KeyData marshalled = adapter.unmarshal(keyData);

        assertThat(marshalled.getPrivateKey()).isEqualTo("LITERAL_PRIVATE");

    }

    @Test
    public void marshallKeysWithLockedPrivateKey() {

        final KeyData keyData = new KeyData(
                new KeyDataConfig(
                        new PrivateKeyData(
                                null,
                                "x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC",
                                "7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=",
                                "d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc",
                                new ArgonOptions("id", 10, 1048576, 4),
                                "q"
                        ),
                        PrivateKeyType.LOCKED
                ),
                null,
                "PUB", null, null, null
        );

        final KeyData marshalled = adapter.unmarshal(keyData);

        assertThat(marshalled.getPrivateKey()).isEqualTo("6ccai0+GXRRVbNckE+JubN+UQ9+8pMCx86dZI683X7w=");

    }

    @Test
    public void fileUnmarshallingSucceeds() throws IOException {

        final String publicKey = "publicKey";
        final String privateKey = "{\"data\":{\"bytes\":\"Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=\"},\"type\":\"unlocked\"}";

        final Path pub = Files.createTempFile("public", ".pub");
        final Path priv = Files.createTempFile("private", ".key");

        Files.write(pub, publicKey.getBytes(UTF_8));
        Files.write(priv, privateKey.getBytes(UTF_8));

        final KeyData keyData = new KeyData(null, null, null, priv, pub, null);

        final KeyData resolved = this.adapter.unmarshal(keyData);

        assertThat(resolved.getPublicKey()).isEqualTo(publicKey);
        assertThat(resolved.getPrivateKey()).isEqualTo("Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=");
        assertThat(resolved.getConfig().getType()).isEqualTo(UNLOCKED);

    }

    @Test
    public void bothPathsMustBeSetIfUsingKeyPathsPrivReturnsAndDoesNotThrowError() throws Exception {
        final Path priv = Files.createTempFile("private", ".key");

        final KeyData badConfig = new KeyData(null, null, null, priv, null, null);

        KeyData result = this.adapter.unmarshal(badConfig);
        assertThat(result).isSameAs(badConfig);
    }

    @Test
    public void bothPathsMustBeSetIfUsingKeyPathsPubReturnsAndDoesNotThrowError() throws Exception {
        final Path pub = Files.createTempFile("public", ".pub");

        final KeyData badConfig = new KeyData(null, null, null, null, pub, null);

        KeyData result = this.adapter.unmarshal(badConfig);
        assertThat(result).isSameAs(badConfig);
    }

    @Test
    public void decryptingPrivateKeyWithWrongPasswordErrorsReturnsAndDoesNotThrowError() {
        final KeyData keyData = new KeyData(
                new KeyDataConfig(
                        new PrivateKeyData(
                                null,
                                "x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC",
                                "7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=",
                                "d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc",
                                new ArgonOptions("id", 10, 1048576, 4),
                                "badpassword"
                        ),
                        PrivateKeyType.LOCKED
                ),
                null,
                "PUB", null, null, null
        );

        KeyData result = this.adapter.unmarshal(keyData);
        assertThat(result.getPrivateKey()).startsWith("NACL_FAILURE");

    }

    @Test
    public void unmarshallingLockedWithNoPasswordFailsReturnsAndDoesNotThrowError() {
        final KeyData keyData = new KeyData(
                new KeyDataConfig(
                        new PrivateKeyData(
                                null,
                                "x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC",
                                "7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=",
                                "d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc",
                                new ArgonOptions("id", 10, 1048576, 4),
                                null
                        ),
                        PrivateKeyType.LOCKED
                ),
                null,
                "PUB", null, null, null
        );

        KeyData result = this.adapter.unmarshal(keyData);
        assertThat(result).isSameAs(keyData);
    }

    @Test
    public void unmarshallingLockedWithNonExistentPublicKeyFileFailsReturnsAndDoesNotThrowError() {

        FilesDelegate filesDelegate = mock(FilesDelegate.class);
        adapter.setFilesDelegate(filesDelegate);

        Path privateKeyPath = mock(Path.class);
        Path publicKeyPath = mock(Path.class);
        when(filesDelegate.notExists(publicKeyPath)).thenReturn(true);
        when(filesDelegate.notExists(privateKeyPath)).thenReturn(false);

        final KeyData keyData = new KeyData(
                new KeyDataConfig(
                        new PrivateKeyData(
                                null,
                                "x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC",
                                "7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=",
                                "d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc",
                                new ArgonOptions("id", 10, 1048576, 4),
                                null
                        ),
                        PrivateKeyType.LOCKED
                ),
                null,
                "PUB", privateKeyPath, publicKeyPath.toAbsolutePath(),
            null
        );

        KeyData result = this.adapter.unmarshal(keyData);
        assertThat(result).isSameAs(keyData);

    }

    @Test
    public void unmarshallingLockedWithNonExistentPrivateKeyFileFailsReturnsAndDoesNotThrowError() {

        FilesDelegate filesDelegate = mock(FilesDelegate.class);
        adapter.setFilesDelegate(filesDelegate);

        Path privateKeyPath = mock(Path.class);
        Path publicKeyPath = mock(Path.class);
        when(filesDelegate.notExists(publicKeyPath)).thenReturn(false);
        when(filesDelegate.notExists(privateKeyPath)).thenReturn(true);

        final KeyData keyData = new KeyData(
                new KeyDataConfig(
                        new PrivateKeyData(
                                null,
                                "x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC",
                                "7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=",
                                "d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc",
                                new ArgonOptions("id", 10, 1048576, 4),
                                null
                        ),
                        PrivateKeyType.LOCKED
                ),
                null,
                "PUB", privateKeyPath, publicKeyPath,
            null
        );

        KeyData result = this.adapter.unmarshal(keyData);
        assertThat(result).isSameAs(keyData);


    }

    @Test
    public void pathsSetDoesntReturnWholeConfig() {
        final KeyData keyData = new KeyData(
            new KeyDataConfig(
                new PrivateKeyData(
                    null,
                    "x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC",
                    "7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=",
                    "d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc",
                    new ArgonOptions("id", 10, 1048576, 4),
                    null
                ),
                PrivateKeyType.LOCKED
            ),
            null,
            "PUB", Paths.get("priv"), Paths.get("pub"),
            null
        );

        final KeyData result = this.adapter.marshal(keyData);

        assertThat(result.getPrivateKeyPath()).isEqualTo(Paths.get("priv"));
        assertThat(result.getPublicKeyPath()).isEqualTo(Paths.get("pub"));
        assertThat(result.getPrivateKey()).isNull();
        assertThat(result.getPublicKey()).isNull();
        assertThat(result.getConfig()).isNull();
    }

    @Test
    public void pathsSetDoesntReturnWholeConfigPublicOnly() {
        final KeyData keyData = new KeyData(
            new KeyDataConfig(
                new PrivateKeyData(
                    null,
                    "x3HUNXH6LQldKtEv3q0h0hR4S12Ur9pC",
                    "7Sem2tc6fjEfW3yYUDN/kSslKEW0e1zqKnBCWbZu2Zw=",
                    "d0CmRus0rP0bdc7P7d/wnOyEW14pwFJmcLbdu2W3HmDNRWVJtoNpHrauA/Sr5Vxc",
                    new ArgonOptions("id", 10, 1048576, 4),
                    null
                ),
                PrivateKeyType.LOCKED
            ),
            null,
            "PUB", null, Paths.get("pub"),
            null
        );

        final KeyData result = this.adapter.marshal(keyData);

        assertThat(result.getPrivateKeyPath()).isNull();
        assertThat(result.getPublicKeyPath()).isEqualTo(Paths.get("pub"));
        assertThat(result.getPrivateKey()).isNull();
        assertThat(result.getPublicKey()).isNull();
        assertThat(result.getConfig()).isNull();
    }

    @Test
    public void unmarshallingPublicOnlyDoesNothing() {
        final KeyData initial = new KeyData(null, null, "publicKey", null, null, null);

        final KeyData result = this.adapter.unmarshal(initial);

        assertThat(result).isEqualTo(initial);
    }

    @Test
    public void unmarshallingPrivateOnlyDoesNothing() {
        final KeyData initial = new KeyData(null, "privateKey", null, null, null, null);

        final KeyData result = this.adapter.unmarshal(initial);

        assertThat(result).isEqualTo(initial);
    }

    @Test
    public void unmarshallingPublicAndPrivateDoesNothing() {
        final KeyData initial = new KeyData(null, "privateKey", "publicKey", null, null, null);

        final KeyData result = this.adapter.unmarshal(initial);

        assertThat(result).isEqualTo(initial);
    }

    @Test
    public void unmarshallingVaultIdOnlyDoesNothing() {
        final KeyData initial = new KeyData(null, null, null, null, null, "vaultId");
        final KeyData result = this.adapter.unmarshal(initial);

        assertThat(result).isEqualTo(initial);
    }

    @Test
    public void unmarshallingConfigOnlyDoesNothing() {
        final KeyData initial = new KeyData(new KeyDataConfig(
            new PrivateKeyData(
                "value", null, null, null, null, null
            ), PrivateKeyType.UNLOCKED
        ), null, null, null, null, null
        );

        final KeyData result = this.adapter.unmarshal(initial);

        assertThat(result).isEqualTo(initial);
    }

    @Test
    public void unmarshallingPublicPathOnlyDoesNothing() throws Exception {
        final String publicKey = "publicKey";

        final Path pub = Files.createTempFile("public", ".pub");

        Files.write(pub, publicKey.getBytes(UTF_8));

        final KeyData initial = new KeyData(null, null, null, null, pub, null);

        final KeyData result = this.adapter.unmarshal(initial);

        assertThat(result).isEqualTo(initial);
    }

    @Test
    public void unmarshallingPrivatePathOnlyDoesNothing() throws Exception {
        final String privateKey = "{\"data\":{\"bytes\":\"Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=\"},\"type\":\"unlocked\"}";

        final Path priv = Files.createTempFile("private", ".key");

        Files.write(priv, privateKey.getBytes(UTF_8));

        final KeyData initial = new KeyData(null, null, null, priv, null, null);

        final KeyData result = this.adapter.unmarshal(initial);

        assertThat(result).isEqualTo(initial);
    }

    @Test
    public void unmarshallingPublicPathAndPrivateConfigDoesNothing() throws Exception {
        final String publicKey = "publicKey";

        final Path pub = Files.createTempFile("public", ".pub");

        Files.write(pub, publicKey.getBytes(UTF_8));

        final KeyDataConfig privateKeyConfig = new KeyDataConfig(
            new PrivateKeyData(
                "Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=",
                null,
                null,
                null,
                null,
                null
            ),
            PrivateKeyType.UNLOCKED
        );

        final KeyData initial = new KeyData(privateKeyConfig, null, null, null, pub, null);

        final KeyData result = this.adapter.unmarshal(initial);

        assertThat(result).isEqualTo(initial);
    }

    @Test
    public void unmarshallingPublicAndVaultIdDoesNothing() {
        final KeyData initial = new KeyData(null, null, "publicKey", null, null, "vaultId");

        final KeyData result = this.adapter.unmarshal(initial);

        assertThat(result).isEqualTo(initial);
    }

    @Test
    public void unmarshallingInlineAndFilesDoesNothing() throws Exception {
        final String publicKey = "publicKey";
        final String privateKey = "{\"data\":{\"bytes\":\"Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=\"},\"type\":\"unlocked\"}";

        final Path pub = Files.createTempFile("public", ".pub");
        final Path priv = Files.createTempFile("private", ".key");

        Files.write(pub, publicKey.getBytes(UTF_8));
        Files.write(priv, privateKey.getBytes(UTF_8));

        final KeyData initial = new KeyData(null, "privInline", "pubInline", priv, pub, null);

        final KeyData result = this.adapter.unmarshal(initial);

        assertThat(result).isEqualTo(initial);
    }

    @Test
    public void unmarshallingPublicWithEmptyKeyDataConfigDoesNothing() {
        final KeyData initial = new KeyData(
            new KeyDataConfig(
                null,
                null
            ),
            null,
            "public",
            null,
            null,
            null
        );

        final KeyData result = this.adapter.unmarshal(initial);

        assertThat(result).isEqualTo(initial);
    }

    @Test
    public void unmarshallingInlineWithFilesWithConfigWithVaultIdDoesNothing() throws Exception {
        final String publicKey = "publicKey";
        final String privateKey = "{\"data\":{\"bytes\":\"Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=\"},\"type\":\"unlocked\"}";

        final Path pub = Files.createTempFile("public", ".pub");
        final Path priv = Files.createTempFile("private", ".key");

        Files.write(pub, publicKey.getBytes(UTF_8));
        Files.write(priv, privateKey.getBytes(UTF_8));

        final KeyData initial = new KeyData(
            new KeyDataConfig(
                new PrivateKeyData(
                    "privateKey",
                    null,null,null,null,null
                ),
                PrivateKeyType.UNLOCKED
            ),
            "privInline", "pubInline", priv, pub, "vaultId");

        final KeyData result = this.adapter.unmarshal(initial);

        assertThat(result).isEqualTo(initial);
    }

    @Test
    public void unmarshallingPublicPathAndPrivatePathGetsKeysFromFiles() throws Exception {
        final String publicKey = "publicKey";
        final String privateKey = "{\"data\":{\"bytes\":\"Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=\"},\"type\":\"unlocked\"}";

        final Path pub = Files.createTempFile("public", ".pub");
        final Path priv = Files.createTempFile("private", ".key");

        Files.write(pub, publicKey.getBytes(UTF_8));
        Files.write(priv, privateKey.getBytes(UTF_8));

        final KeyData initial = new KeyData(null, null, null, priv, pub, null);

        final KeyData result = this.adapter.unmarshal(initial);

        assertThat(result.getPublicKey()).isEqualTo(publicKey);
        assertThat(result.getPrivateKey()).isEqualTo("Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=");
    }

    @Test
    public void unmarshallingPublicAndPrivateConfigGetsPrivateKeyFromConfig() {
        final String publicKey = "publicKey";

        final KeyDataConfig privateKeyConfig = new KeyDataConfig(
            new PrivateKeyData(
                "Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=",
                null,
                null,
                null,
                null,
                null
            ),
            PrivateKeyType.UNLOCKED
        );

        final KeyData initial = new KeyData(privateKeyConfig, null, publicKey, null, null, null);

        final KeyData result = this.adapter.unmarshal(initial);

        assertThat(result.getPublicKey()).isEqualTo(publicKey);
        assertThat(result.getPrivateKey()).isEqualTo("Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=");
    }

    @Test
    public void unmarshallingPublicPathAndVaultIdGetsKeyFromFile() throws Exception {
        final String publicKey = "publicKey";

        final Path pub = Files.createTempFile("public", ".pub");

        Files.write(pub, publicKey.getBytes(UTF_8));

        final KeyData initial = new KeyData(null, null, null, null, pub, "vaultId");

        final KeyData result = this.adapter.unmarshal(initial);

        assertThat(result.getKeyVaultId()).isEqualTo("vaultId");
        assertThat(result.getPublicKey()).isEqualTo(publicKey);
    }

    @Test
    public void unmarshallingPathsWithConfigGetsKeysFromFile() throws Exception {
        final String publicKey = "publicKey";
        final String privateKey = "{\"data\":{\"bytes\":\"Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=\"},\"type\":\"unlocked\"}";

        final Path pub = Files.createTempFile("public", ".pub");
        final Path priv = Files.createTempFile("private", ".key");

        Files.write(pub, publicKey.getBytes(UTF_8));
        Files.write(priv, privateKey.getBytes(UTF_8));

        KeyDataConfig keyDataConfig = new KeyDataConfig(
            new PrivateKeyData(
                "privateKey",
                null,null,null,null,null
            ),
            PrivateKeyType.UNLOCKED
        );

        final KeyData initial = new KeyData(keyDataConfig,null, null, priv, pub, null);

        final KeyData result = this.adapter.unmarshal(initial);

        assertThat(result.getPublicKey()).isEqualTo(publicKey);
        assertThat(result.getPrivateKey()).isEqualTo("Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=");
    }
}
