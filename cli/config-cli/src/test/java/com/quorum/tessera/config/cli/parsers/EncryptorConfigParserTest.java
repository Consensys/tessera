package com.quorum.tessera.config.cli.parsers;

import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.EncryptorType;
import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import static org.assertj.core.api.Assertions.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class EncryptorConfigParserTest {

    private EncryptorConfigParser parser;

    private CommandLine commandLine;

    @Before
    public void onSetup() {
        this.parser = new EncryptorConfigParser();
        commandLine = mock(CommandLine.class);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(commandLine);
    }

    @Test
    public void elipticalCurveNoPropertiesDefined() throws IOException {
        Config config = new Config();
        config.setEncryptor(new EncryptorConfig());
        config.getEncryptor().setType(EncryptorType.AEC);
        when(commandLine.hasOption("configfile")).thenReturn(false);
        when(commandLine.hasOption("encryptor.type")).thenReturn(true);
        when(commandLine.getOptionValue(anyString())).thenReturn(null);
        when(commandLine.getOptionValue("encryptor.type")).thenReturn(EncryptorType.AEC.name());

        EncryptorConfig result = parser.parse(commandLine);

        assertThat(result.getType()).isEqualTo(EncryptorType.AEC);
        assertThat(result.getProperties()).isEmpty();

        verify(commandLine).getOptionValue("encryptor.type");
        verify(commandLine, times(5)).getOptionValue(anyString());
        verify(commandLine).hasOption("encryptor.type");
        verify(commandLine).hasOption("configfile");
    }

    @Test
    public void elipticalCurveWithDefinedProperties() throws IOException {
        Config config = new Config();
        config.setEncryptor(new EncryptorConfig());
        config.getEncryptor().setType(EncryptorType.AEC);

        when(commandLine.hasOption("configfile")).thenReturn(false);
        when(commandLine.hasOption("encryptor.type")).thenReturn(true);

        when(commandLine.getOptionValue("encryptor.type"))
                .thenReturn(EncryptorType.AEC.name());

        when(commandLine.getOptionValue("encryptor.symmetricCipher")).thenReturn("somecipher");
        when(commandLine.getOptionValue("encryptor.ellipticCurve")).thenReturn("somecurve");
        when(commandLine.getOptionValue("encryptor.nonceLength")).thenReturn("3");
        when(commandLine.getOptionValue("encryptor.sharedKeyLength")).thenReturn("2");

        EncryptorConfig result = parser.parse(commandLine);

        assertThat(result.getType()).isEqualTo(EncryptorType.AEC);
        assertThat(result.getProperties()).containsOnlyKeys("symmetricCipher", "ellipticCurve", "nonceLength", "sharedKeyLength");

        assertThat(result.getProperties().get("symmetricCipher")).isEqualTo("somecipher");
        assertThat(result.getProperties().get("ellipticCurve")).isEqualTo("somecurve");
        assertThat(result.getProperties().get("nonceLength")).isEqualTo("3");
        assertThat(result.getProperties().get("sharedKeyLength")).isEqualTo("2");

        verify(commandLine).getOptionValue("encryptor.symmetricCipher");
        verify(commandLine).getOptionValue("encryptor.ellipticCurve");
        verify(commandLine).getOptionValue("encryptor.nonceLength");
        verify(commandLine).getOptionValue("encryptor.sharedKeyLength");

        verify(commandLine).getOptionValue("encryptor.type");
        verify(commandLine).hasOption("encryptor.type");
        verify(commandLine).hasOption("configfile");
    }

    @Test
    public void noEncryptorTypeDefined() throws IOException {

        try {
            parser.parse(commandLine);
            failBecauseExceptionWasNotThrown(CliException.class);
        } catch (CliException ex) {
            verify(commandLine,times(2)).hasOption(anyString());
        }

    }

}
