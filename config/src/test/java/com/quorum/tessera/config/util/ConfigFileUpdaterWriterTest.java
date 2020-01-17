package com.quorum.tessera.config.util;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keypairs.DirectKeyPair;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.keys.KeyEncryptorHolder;
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

    @Before
    public void setUp() {
        filesDelegate = mock(FilesDelegate.class);
        writer = new ConfigFileUpdaterWriter(filesDelegate);
    }

    @Test
    public void appendNewKeysToExistingAndWrite() throws Exception {
        String pub1 = "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=";
        String priv1 = "Wl+xSyXVuuqzpvznOS7dOobhcn4C5auxkFRi7yLtgtA=";

        String pub2 = "eKSr3gAw5zPubOK96dw2qkZZmIT2HUBf8Zv001ubBC0=";
        String priv2 = "9YabvoCKGD1sUkUgHak4XJzbtgdwEsF/jiarLoTHeR0=";

        ConfigKeyPair key1 = new DirectKeyPair(pub1, priv1);
        ConfigKeyPair key2 = new DirectKeyPair(pub2, priv2);

        List<ConfigKeyPair> newKeys = Collections.singletonList(key1);
        List<ConfigKeyPair> existingKeys = new ArrayList<>(Collections.singletonList(key2));

        Config config = JaxbUtil.unmarshal(getClass().getResourceAsStream("/sample.json"), Config.class);
        config.getKeys().setKeyData(existingKeys);
        config.getKeys().setPasswords(null);

        Path configDest = mock(Path.class);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        KeyEncryptor keyEncryptor = KeyEncryptorFactory.newFactory().create(EncryptorConfig.getDefault());
        KeyEncryptorHolder.INSTANCE.setKeyEncryptor(keyEncryptor);
        when(filesDelegate.newOutputStream(configDest, CREATE_NEW)).thenReturn(out);

        writer.updateAndWrite(newKeys, config, configDest);

        verify(filesDelegate).newOutputStream(configDest, CREATE_NEW);

        Config updated = JaxbUtil.unmarshal(new ByteArrayInputStream(out.toByteArray()), Config.class);
        assertThat(updated).isNotNull();
        assertThat(updated.getKeys().getKeyData()).hasSize(2);
        assertThat(updated.getKeys().getKeyData()).usingFieldByFieldElementComparator().containsExactlyInAnyOrder(key1, key2);
    }

    @Test
    public void cleansUpIfExceptionThrown() {
        Config config = mock(Config.class);
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        when(config.getKeys()).thenReturn(keyConfiguration);
        when(keyConfiguration.getKeyData()).thenReturn(new ArrayList<>());

        when(filesDelegate.newOutputStream(any(), any())).thenThrow(new RuntimeException());

        Path configDest = mock(Path.class);

        Throwable ex = catchThrowable(() -> writer.updateAndWrite(new ArrayList<>(), config, configDest));

        assertThat(ex).isNotNull();
        verify(filesDelegate).deleteIfExists(configDest);
    }
}
