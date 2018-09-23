package com.quorum.tessera.config.adapters;

import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keypairs.FilesystemKeyPair;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class KeyConfigurationAdapterTest {

    private KeyConfigurationAdapter keyConfigurationAdapter = new KeyConfigurationAdapter();

    @Test
    public void marshallingDoesNothing() {

        final KeyConfiguration keyConfiguration = new KeyConfiguration(null, null, emptyList());

        final KeyConfiguration marshalled = this.keyConfigurationAdapter.marshal(keyConfiguration);

        assertThat(marshalled).isSameAs(keyConfiguration);

    }

    @Test
    public void emptyPasswordsReturnsSameKeys() {

        //null paths since we won't actually be reading them
        final ConfigKeyPair keypair = new FilesystemKeyPair(null, null);
        final KeyConfiguration keyConfiguration = new KeyConfiguration(null, emptyList(), singletonList(keypair));

        final KeyConfiguration configuration = this.keyConfigurationAdapter.unmarshal(keyConfiguration);

        assertThat(configuration.getKeyData()).hasSize(1);
        final ConfigKeyPair returned = configuration.getKeyData().get(0);

        //passwords are always non-null, set to empty string if not present or not needed
        assertThat(returned.getPassword()).isEqualTo("");
        assertThat(returned).isSameAs(keypair);
    }

    @Test
    public void noPasswordsReturnsSameKeys() {

        //null paths since we won't actually be reading them
        final ConfigKeyPair keypair = new FilesystemKeyPair(null, null);
        final KeyConfiguration keyConfiguration = new KeyConfiguration(null, null, singletonList(keypair));

        final KeyConfiguration configuration = this.keyConfigurationAdapter.unmarshal(keyConfiguration);

        assertThat(configuration.getKeyData()).hasSize(1);
        final ConfigKeyPair returned = configuration.getKeyData().get(0);

        //passwords are always non-null, set to empty string if not present or not needed
        assertThat(returned.getPassword()).isEqualTo("");
        assertThat(returned).isSameAs(keypair);
    }

    @Test
    public void passwordsAssignedToKeys() {

        //null paths since we won't actually be reading them
        final ConfigKeyPair keypair = new FilesystemKeyPair(null, null);
        final KeyConfiguration keyConfiguration
            = new KeyConfiguration(null, singletonList("passwordsAssignedToKeys"), singletonList(keypair));

        final KeyConfiguration configuration = this.keyConfigurationAdapter.unmarshal(keyConfiguration);

        assertThat(configuration.getKeyData()).hasSize(1);
        final ConfigKeyPair returned = configuration.getKeyData().get(0);
        assertThat(returned.getPassword()).isEqualTo("passwordsAssignedToKeys");
    }

    @Test
    public void unreadablePasswordFileGivesNoPasswords() throws IOException {

        final Path passes = Files.createTempDirectory("testdirectory").resolve("nonexistantfile.txt");

        final ConfigKeyPair keypair = new FilesystemKeyPair(null, null);
        final KeyConfiguration keyConfiguration = new KeyConfiguration(passes, null, singletonList(keypair));

        final KeyConfiguration configuration = this.keyConfigurationAdapter.unmarshal(keyConfiguration);

        assertThat(configuration.getKeyData()).hasSize(1);
        final ConfigKeyPair returned = configuration.getKeyData().get(0);
        assertThat(returned.getPassword()).isEqualTo("");

    }

}
