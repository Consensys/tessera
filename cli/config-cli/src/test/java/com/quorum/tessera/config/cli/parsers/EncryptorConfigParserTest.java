package com.quorum.tessera.config.cli.parsers;

import com.quorum.tessera.io.FilesDelegate;
import org.apache.commons.cli.CommandLine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class EncryptorConfigParserTest {

    private EncryptorConfigParser parser;

    private CommandLine commandLine;

    private FilesDelegate filesDelegate;

    @Before
    public void onSetup() {
        commandLine = mock(CommandLine.class);
        filesDelegate = mock(FilesDelegate.class);
        this.parser = new EncryptorConfigParser(filesDelegate);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(commandLine);
    }

    @Test
    public void constructor() {
        parser = new EncryptorConfigParser();
        assertThat(parser).isNotNull();
    }

    //    @Test
    //    public void ellipticalCurveNoPropertiesDefined() throws IOException {
    //        when(commandLine.hasOption("configfile")).thenReturn(false);
    //
    //        when(commandLine.getOptionValue("encryptor.type", EncryptorType.NACL.name()))
    //                .thenReturn(EncryptorType.EC.name());
    //
    //        EncryptorConfig result = parser.parse(commandLine);
    //
    //        assertThat(result.getType()).isEqualTo(EncryptorType.EC);
    //        assertThat(result.getProperties()).isEmpty();
    //
    //        verify(commandLine).getOptionValue("encryptor.type", EncryptorType.NACL.name());
    //        verify(commandLine).hasOption("configfile");
    //
    //        verify(commandLine).getOptionValue("encryptor.symmetricCipher");
    //        verify(commandLine).getOptionValue("encryptor.ellipticCurve");
    //        verify(commandLine).getOptionValue("encryptor.nonceLength");
    //        verify(commandLine).getOptionValue("encryptor.sharedKeyLength");
    //    }
    //
    //    @Test
    //    public void ellipticalCurveWithDefinedProperties() throws IOException {
    //
    //        when(commandLine.getOptionValue("encryptor.type", EncryptorType.NACL.name()))
    //                .thenReturn(EncryptorType.EC.name());
    //
    //        Config config = new Config();
    //        config.setEncryptor(new EncryptorConfig());
    //        config.getEncryptor().setType(EncryptorType.EC);
    //
    //        when(commandLine.hasOption("configfile")).thenReturn(false);
    //
    //        when(commandLine.getOptionValue("encryptor.type")).thenReturn(EncryptorType.EC.name());
    //
    //        when(commandLine.getOptionValue("encryptor.symmetricCipher")).thenReturn("somecipher");
    //        when(commandLine.getOptionValue("encryptor.ellipticCurve")).thenReturn("somecurve");
    //        when(commandLine.getOptionValue("encryptor.nonceLength")).thenReturn("3");
    //        when(commandLine.getOptionValue("encryptor.sharedKeyLength")).thenReturn("2");
    //
    //        EncryptorConfig result = parser.parse(commandLine);
    //
    //        assertThat(result.getType()).isEqualTo(EncryptorType.EC);
    //        assertThat(result.getProperties())
    //                .containsOnlyKeys("symmetricCipher", "ellipticCurve", "nonceLength", "sharedKeyLength");
    //
    //        assertThat(result.getProperties().get("symmetricCipher")).isEqualTo("somecipher");
    //        assertThat(result.getProperties().get("ellipticCurve")).isEqualTo("somecurve");
    //        assertThat(result.getProperties().get("nonceLength")).isEqualTo("3");
    //        assertThat(result.getProperties().get("sharedKeyLength")).isEqualTo("2");
    //
    //        verify(commandLine).getOptionValue("encryptor.symmetricCipher");
    //        verify(commandLine).getOptionValue("encryptor.ellipticCurve");
    //        verify(commandLine).getOptionValue("encryptor.nonceLength");
    //        verify(commandLine).getOptionValue("encryptor.sharedKeyLength");
    //
    //        verify(commandLine).getOptionValue("encryptor.type", EncryptorType.NACL.name());
    //        verify(commandLine).hasOption("configfile");
    //    }
    //
    //    @Test
    //    public void noEncryptorTypeDefinedAndNoConfigFile() throws IOException {
    //
    //        when(commandLine.getOptionValue("encryptor.type", EncryptorType.NACL.name()))
    //                .thenReturn(EncryptorType.NACL.name());
    //        when(commandLine.hasOption("configfile")).thenReturn(false);
    //        EncryptorConfig result = parser.parse(commandLine);
    //        assertThat(result.getType()).isEqualTo(EncryptorType.NACL);
    //        assertThat(result.getProperties()).isEmpty();
    //
    //        verify(commandLine).getOptionValue("encryptor.type", EncryptorType.NACL.name());
    //        verify(commandLine).hasOption("configfile");
    //    }
    //
    //    @Test
    //    public void keyGenUsedDefaulIfNoTypeDefined() throws Exception {
    //        when(commandLine.getOptionValue("encryptor.type", EncryptorType.NACL.name()))
    //                .thenReturn(EncryptorType.NACL.name());
    //        when(commandLine.hasOption("configfile")).thenReturn(true);
    //        when(commandLine.getOptionValue("configfile")).thenReturn("somepath");
    //
    //        InputStream inputStream = new ByteArrayInputStream("{}".getBytes());
    //        when(filesDelegate.newInputStream(any(Path.class))).thenReturn(inputStream);
    //
    //        EncryptorConfig result = parser.parse(commandLine);
    //
    //        assertThat(result.getType()).isEqualTo(EncryptorType.NACL);
    //        assertThat(result.getProperties()).isEmpty();
    //
    //        verify(commandLine).getOptionValue("encryptor.type", EncryptorType.NACL.name());
    //        verify(commandLine).getOptionValue("configfile");
    //        verify(commandLine).hasOption("configfile");
    //
    //        verify(filesDelegate).newInputStream(any(Path.class));
    //    }
    //
    //    @Test
    //    public void encryptorConfigFromFile() throws Exception {
    //        when(commandLine.getOptionValue("encryptor.type", EncryptorType.NACL.name()))
    //                .thenReturn(EncryptorType.NACL.name());
    //        when(commandLine.hasOption("configfile")).thenReturn(true);
    //        when(commandLine.getOptionValue("configfile")).thenReturn("somepath");
    //
    //        String jsonConfig = "{\"encryptor\": {\"type\": \"EC\"}}";
    //
    //        InputStream inputStream = new ByteArrayInputStream(jsonConfig.getBytes());
    //        when(filesDelegate.newInputStream(any(Path.class))).thenReturn(inputStream);
    //
    //        EncryptorConfig result = parser.parse(commandLine);
    //
    //        assertThat(result.getType()).isEqualTo(EncryptorType.EC);
    //
    //        verify(commandLine).getOptionValue("encryptor.type", EncryptorType.NACL.name());
    //        verify(commandLine).hasOption("configfile");
    //        verify(commandLine).getOptionValue("configfile");
    //        verify(filesDelegate).newInputStream(any(Path.class));
    //
    //        verifyNoMoreInteractions(filesDelegate);
    //    }
}
