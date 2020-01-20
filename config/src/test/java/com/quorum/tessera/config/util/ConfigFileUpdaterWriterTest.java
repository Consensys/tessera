package com.quorum.tessera.config.util;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keypairs.DirectKeyPair;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.io.FilesDelegate;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

public class ConfigFileUpdaterWriterTest {

    private FilesDelegate filesDelegate;

    private ConfigFileUpdaterWriter writer;

    private final KeyEncryptor keyEncryptor = KeyEncryptorFactory.newFactory().create(EncryptorConfig.getDefault());

    @Before
    public void setUp() {
        filesDelegate = mock(FilesDelegate.class);
        writer = new ConfigFileUpdaterWriter(filesDelegate);
    }

    @Test
    public void testMarshalling() {
        final Config config = JaxbUtil.unmarshal(getClass().getResourceAsStream("/sample_full.json"), Config.class);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        JaxbUtil.marshal(config, out);

        final ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

        final Config config2 = JaxbUtil.unmarshal(in, Config.class);

        assertThat(config2).isNotNull();
    }

    @Test
    public void appendNewKeysToExistingAndWrite() throws Exception {
        final String pub1 = "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=";
        final String priv1 = "Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=";

        final String pub2 = "eKSr3gAw5zPubOK96dw2qkZZmIT2HUBf8Zv001ubBC0=";
        final String priv2 = "9YabvoCKGD1sUkUgHak4XJzbtgdwEsF/jiarLoTHeR0=";

        final ConfigKeyPair key1 = new DirectKeyPair(pub1, priv1);
        final ConfigKeyPair key2 = new DirectKeyPair(pub2, priv2);

        final List<ConfigKeyPair> newKeys = Collections.singletonList(key1);
        final List<ConfigKeyPair> existingKeys = new ArrayList<>(Collections.singletonList(key2));

        final Config config = JaxbUtil.unmarshal(getClass().getResourceAsStream("/sample_full.json"), Config.class);
        config.getKeys().setKeyData(existingKeys);
        config.getKeys().setPasswords(null);

        final Path configDest = mock(Path.class);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(filesDelegate.newOutputStream(configDest, CREATE_NEW)).thenReturn(out);

        writer.updateAndWrite(newKeys, null, config, configDest);

        verify(filesDelegate).newOutputStream(configDest, CREATE_NEW);

        final Config updated = JaxbUtil.unmarshal(new ByteArrayInputStream(out.toByteArray()), Config.class);
        assertThat(updated).isNotNull();
        assertThat(updated.getKeys().getKeyData()).hasSize(2);
        assertThat(updated.getKeys().getKeyData())
                .usingFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(key1, key2);
    }

    @Test
    public void addsKeyVaultConfigBeforeWriting() throws Exception {
        final String pub1 = "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=";
        final String priv1 = "Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=";

        final ConfigKeyPair key1 = new DirectKeyPair(pub1, priv1);

        final List<ConfigKeyPair> newKeys = Collections.singletonList(key1);

        final Config config = JaxbUtil.unmarshal(getClass().getResourceAsStream("/sample_full.json"), Config.class);
        config.getKeys().setPasswords(null);

        final Path configDest = mock(Path.class);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(filesDelegate.newOutputStream(configDest, CREATE_NEW)).thenReturn(out);

        KeyVaultConfig keyVaultConfig = new AzureKeyVaultConfig("someurl");

        writer.updateAndWrite(newKeys, keyVaultConfig, config, configDest);

        verify(filesDelegate).newOutputStream(configDest, CREATE_NEW);

        final Config updated = JaxbUtil.unmarshal(new ByteArrayInputStream(out.toByteArray()), Config.class);
        assertThat(updated).isNotNull();
        assertThat(updated.getKeys().getKeyVaultConfigs()).hasSize(1);

        DefaultKeyVaultConfig wantKeyVaultConfig = new DefaultKeyVaultConfig();
        wantKeyVaultConfig.setKeyVaultType(KeyVaultType.AZURE);
        wantKeyVaultConfig.setProperty("url", "someurl");

        assertThat(updated.getKeys().getKeyVaultConfigs())
                .usingFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(wantKeyVaultConfig);
    }

    @Test
    public void doesNotAddMoreThanOneKeyVaultConfigOfTheSameType() throws Exception {
        final String pub1 = "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=";
        final String priv1 = "Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=";

        final ConfigKeyPair key1 = new DirectKeyPair(pub1, priv1);

        final List<ConfigKeyPair> newKeys = Collections.singletonList(key1);

        final Config config = JaxbUtil.unmarshal(getClass().getResourceAsStream("/sample_full.json"), Config.class);
        config.getKeys().setPasswords(null);
        config.getKeys().addKeyVaultConfig(new AzureKeyVaultConfig("already have an azure config"));

        final Path configDest = mock(Path.class);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(filesDelegate.newOutputStream(configDest, CREATE_NEW)).thenReturn(out);

        KeyVaultConfig keyVaultConfig = new AzureKeyVaultConfig("will not be added");

        writer.updateAndWrite(newKeys, keyVaultConfig, config, configDest);

        verify(filesDelegate).newOutputStream(configDest, CREATE_NEW);

        final Config updated = JaxbUtil.unmarshal(new ByteArrayInputStream(out.toByteArray()), Config.class);
        assertThat(updated).isNotNull();
        assertThat(updated.getKeys().getKeyVaultConfigs()).hasSize(1);

        DefaultKeyVaultConfig wantKeyVaultConfig = new DefaultKeyVaultConfig();
        wantKeyVaultConfig.setKeyVaultType(KeyVaultType.AZURE);
        wantKeyVaultConfig.setProperty("url", "already have an azure config");

        assertThat(updated.getKeys().getKeyVaultConfigs())
                .usingFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(wantKeyVaultConfig);
    }

    @Test
    public void cleansUpIfExceptionThrown() {
        final Config config = mock(Config.class);
        final KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        when(config.getKeys()).thenReturn(keyConfiguration);
        when(keyConfiguration.getKeyData()).thenReturn(new ArrayList<>());

        when(filesDelegate.newOutputStream(any(), any())).thenThrow(new RuntimeException());

        final Path configDest = mock(Path.class);

        final Throwable ex = catchThrowable(() -> writer.updateAndWrite(new ArrayList<>(), null, config, configDest));

        assertThat(ex).isNotNull();
        verify(filesDelegate).deleteIfExists(configDest);
    }
}
