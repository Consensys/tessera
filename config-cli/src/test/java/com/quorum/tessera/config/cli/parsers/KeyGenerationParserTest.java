package com.quorum.tessera.config.cli.parsers;

import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keys.KeyGenerator;
import com.quorum.tessera.config.keys.MockKeyGeneratorFactory;
import org.apache.commons.cli.CommandLine;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class KeyGenerationParserTest {

    private KeyGenerationParser parser = new KeyGenerationParser();

    @Test
    public void notProvidingArgonOptionsGivesNull() throws Exception {
        final Path keyLocation = Files.createTempFile(UUID.randomUUID().toString(), "");

        final CommandLine commandLine = mock(CommandLine.class);
        when(commandLine.hasOption("keygen")).thenReturn(true);
        when(commandLine.hasOption("filename")).thenReturn(true);
        when(commandLine.getOptionValue("filename")).thenReturn(keyLocation.toString());
        when(commandLine.hasOption("keygenconfig")).thenReturn(false);

        final List<ConfigKeyPair> result = parser.parse(commandLine);

        assertThat(result).isNotNull().hasSize(1);

        final KeyGenerator keyGenerator = MockKeyGeneratorFactory.getMockKeyGenerator();

        final ArgumentCaptor<ArgonOptions> captor = ArgumentCaptor.forClass(ArgonOptions.class);
        verify(keyGenerator).generate(eq(keyLocation.toString()), captor.capture());

        assertThat(captor.getAllValues()).hasSize(1);
        assertThat(captor.getValue()).isNull();
    }

    @Test
    public void providingArgonOptionsGetSentCorrectly() throws Exception {
        final String options = "{\"variant\": \"id\",\"memory\": 100,\"iterations\": 7,\"parallelism\": 22}";
        final Path argonOptions = Files.createTempFile(UUID.randomUUID().toString(), "");
        Files.write(argonOptions, options.getBytes());

        final Path keyLocation = Files.createTempFile(UUID.randomUUID().toString(), "");

        final CommandLine commandLine = mock(CommandLine.class);
        when(commandLine.hasOption("keygen")).thenReturn(true);
        when(commandLine.hasOption("filename")).thenReturn(true);
        when(commandLine.getOptionValue("filename")).thenReturn(keyLocation.toString());
        when(commandLine.hasOption("keygenconfig")).thenReturn(true);
        when(commandLine.getOptionValue("keygenconfig")).thenReturn(argonOptions.toString());

        final List<ConfigKeyPair> result = parser.parse(commandLine);

        assertThat(result).isNotNull().hasSize(1);

        final KeyGenerator keyGenerator = MockKeyGeneratorFactory.getMockKeyGenerator();

        final ArgumentCaptor<ArgonOptions> captor = ArgumentCaptor.forClass(ArgonOptions.class);
        verify(keyGenerator).generate(eq(keyLocation.toString()), captor.capture());

        assertThat(captor.getAllValues()).hasSize(1);
        assertThat(captor.getValue().getAlgorithm()).isEqualTo("id");
        assertThat(captor.getValue().getIterations()).isEqualTo(7);
        assertThat(captor.getValue().getMemory()).isEqualTo(100);
        assertThat(captor.getValue().getParallelism()).isEqualTo(22);
    }

    @Test
    public void keygenWithNoName() throws Exception {

        final CommandLine commandLine = mock(CommandLine.class);
        when(commandLine.hasOption("keygen")).thenReturn(true);
        when(commandLine.hasOption("filename")).thenReturn(false);
        when(commandLine.hasOption("keygenconfig")).thenReturn(false);

        final List<ConfigKeyPair> result = this.parser.parse(commandLine);

        assertThat(result).isNotNull().hasSize(1);

        final KeyGenerator keyGenerator = MockKeyGeneratorFactory.getMockKeyGenerator();
        verify(keyGenerator).generate("", null);
    }

    @Test
    public void keygenNotGivenReturnsEmptyList() throws IOException {

        final CommandLine commandLine = mock(CommandLine.class);
        when(commandLine.hasOption("keygen")).thenReturn(false);
        when(commandLine.hasOption("filename")).thenReturn(false);
        when(commandLine.hasOption("keygenconfig")).thenReturn(false);

        final List<ConfigKeyPair> result = this.parser.parse(commandLine);

        assertThat(result).isNotNull().hasSize(0);

        final KeyGenerator keyGenerator = MockKeyGeneratorFactory.getMockKeyGenerator();
        verifyZeroInteractions(keyGenerator);
    }

}
