package com.quorum.tessera.config.cli.parsers;

import com.quorum.tessera.cli.CliException;
import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.EncryptorType;
import com.quorum.tessera.config.cli.keys.MockKeyGeneratorFactory;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.key.generation.KeyGenerator;
import org.apache.commons.cli.CommandLine;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class KeyGenerationParserTest {

    private KeyGenerationParser parser =
            new KeyGenerationParser(
                    new EncryptorConfig() {
                        {
                            setType(EncryptorType.NACL);
                            setProperties(Collections.emptyMap());
                        }
                    });

    private CommandLine commandLine = mock(CommandLine.class);

    @Test
    public void notProvidingArgonOptionsGivesNull() throws Exception {
        final Path keyLocation = Files.createTempFile(UUID.randomUUID().toString(), "");

        when(commandLine.hasOption("keygen")).thenReturn(true);
        when(commandLine.hasOption("filename")).thenReturn(true);
        when(commandLine.getOptionValue("filename")).thenReturn(keyLocation.toString());
        when(commandLine.hasOption("keygenconfig")).thenReturn(false);

        final List<ConfigKeyPair> result = parser.parse(commandLine);

        assertThat(result).isNotNull().hasSize(1);

        final KeyGenerator keyGenerator = MockKeyGeneratorFactory.getMockKeyGenerator();

        final ArgumentCaptor<ArgonOptions> captor = ArgumentCaptor.forClass(ArgonOptions.class);
        verify(keyGenerator).generate(eq(keyLocation.toString()), captor.capture(), eq(null));

        assertThat(captor.getAllValues()).hasSize(1);
        assertThat(captor.getValue()).isNull();
    }

    @Test
    public void providingArgonOptionsGetSentCorrectly() throws Exception {
        final String options = "{\"variant\": \"id\",\"memory\": 100,\"iterations\": 7,\"parallelism\": 22}";
        final Path argonOptions = Files.createTempFile(UUID.randomUUID().toString(), "");
        Files.write(argonOptions, options.getBytes());

        final Path keyLocation = Files.createTempFile(UUID.randomUUID().toString(), "");

        when(commandLine.hasOption("keygen")).thenReturn(true);
        when(commandLine.hasOption("filename")).thenReturn(true);
        when(commandLine.getOptionValue("filename")).thenReturn(keyLocation.toString());
        when(commandLine.hasOption("keygenconfig")).thenReturn(true);
        when(commandLine.getOptionValue("keygenconfig")).thenReturn(argonOptions.toString());

        final List<ConfigKeyPair> result = parser.parse(commandLine);

        assertThat(result).isNotNull().hasSize(1);

        final KeyGenerator keyGenerator = MockKeyGeneratorFactory.getMockKeyGenerator();

        final ArgumentCaptor<ArgonOptions> captor = ArgumentCaptor.forClass(ArgonOptions.class);
        verify(keyGenerator).generate(eq(keyLocation.toString()), captor.capture(), eq(null));

        assertThat(captor.getAllValues()).hasSize(1);
        assertThat(captor.getValue().getAlgorithm()).isEqualTo("id");
        assertThat(captor.getValue().getIterations()).isEqualTo(7);
        assertThat(captor.getValue().getMemory()).isEqualTo(100);
        assertThat(captor.getValue().getParallelism()).isEqualTo(22);
    }

    @Test
    public void keygenWithNoName() throws Exception {

        when(commandLine.hasOption("keygen")).thenReturn(true);
        when(commandLine.hasOption("filename")).thenReturn(false);
        when(commandLine.hasOption("keygenconfig")).thenReturn(false);

        final List<ConfigKeyPair> result = this.parser.parse(commandLine);

        assertThat(result).isNotNull().hasSize(1);

        final KeyGenerator keyGenerator = MockKeyGeneratorFactory.getMockKeyGenerator();
        verify(keyGenerator).generate("", null, null);
    }

    @Test
    public void keygenNotGivenReturnsEmptyList() throws Exception {

        when(commandLine.hasOption("keygen")).thenReturn(false);
        when(commandLine.hasOption("filename")).thenReturn(false);
        when(commandLine.hasOption("keygenconfig")).thenReturn(false);

        final List<ConfigKeyPair> result = this.parser.parse(commandLine);

        assertThat(result).isNotNull().hasSize(0);

        final KeyGenerator keyGenerator = MockKeyGeneratorFactory.getMockKeyGenerator();
        verifyZeroInteractions(keyGenerator);
    }

    @Test
    public void vaultOptionsNotUsedIfNoneProvided() throws Exception {
        when(commandLine.hasOption("keygenvaulttype")).thenReturn(false);
        when(commandLine.hasOption("keygenvaulturl")).thenReturn(false);

        this.parser.parse(commandLine);

        verify(commandLine, times(0)).getOptionValue("keygenvaulttype");
        verify(commandLine, times(0)).getOptionValue("keygenvaulturl");
    }

    @Test
    public void ifAllVaultOptionsProvidedAndValidThenOkay() throws Exception {
        when(commandLine.hasOption("keygenvaulttype")).thenReturn(true);
        when(commandLine.hasOption("keygenvaulturl")).thenReturn(true);
        when(commandLine.getOptionValue("keygenvaulturl")).thenReturn("someurl");
        when(commandLine.getOptionValue("keygenvaulttype")).thenReturn("AZURE");

        this.parser.parse(commandLine);

        verify(commandLine, times(1)).getOptionValue("keygenvaulttype");
        verify(commandLine, times(1)).getOptionValue("keygenvaulturl");
    }

    @Test
    public void ifAzureVaultTypeOptionProvidedButNoVaultUrlThenValidationException() {
        when(commandLine.hasOption("keygenvaulttype")).thenReturn(true);
        when(commandLine.hasOption("keygenvaulturl")).thenReturn(false);
        when(commandLine.getOptionValue("keygenvaulttype")).thenReturn("AZURE");

        Throwable ex = catchThrowable(() -> this.parser.parse(commandLine));

        verify(commandLine, times(1)).getOptionValue("keygenvaulttype");
        verify(commandLine, times(1)).getOptionValue("keygenvaulturl");

        assertThat(ex).isInstanceOf(ConstraintViolationException.class);

        Set<ConstraintViolation<?>> violations = ((ConstraintViolationException) ex).getConstraintViolations();

        assertThat(violations.size()).isEqualTo(1);

        ConstraintViolation violation = violations.iterator().next();

        assertThat(violation.getPropertyPath().toString()).isEqualTo("url");
        assertThat(violation.getMessage()).isEqualTo("may not be null");
    }

    @Test
    public void ifHashicorpVaultTypeOptionAndFilenameProvidedButNoVaultUrlThenValidationException() {
        when(commandLine.hasOption("keygenvaulttype")).thenReturn(true);
        when(commandLine.hasOption("keygenvaulturl")).thenReturn(false);
        when(commandLine.getOptionValue("keygenvaulttype")).thenReturn("HASHICORP");
        when(commandLine.hasOption("filename")).thenReturn(true);
        when(commandLine.getOptionValue("filename")).thenReturn("secret/path");

        Throwable ex = catchThrowable(() -> this.parser.parse(commandLine));

        verify(commandLine, times(1)).getOptionValue("keygenvaulttype");
        verify(commandLine, times(1)).getOptionValue("keygenvaulturl");

        assertThat(ex).isInstanceOf(ConstraintViolationException.class);

        Set<ConstraintViolation<?>> violations = ((ConstraintViolationException) ex).getConstraintViolations();

        assertThat(violations.size()).isEqualTo(1);

        ConstraintViolation violation = violations.iterator().next();

        assertThat(violation.getPropertyPath().toString()).isEqualTo("url");
        assertThat(violation.getMessage()).isEqualTo("may not be null");
    }

    @Test
    public void ifOnlyVaultUrlOptionProvidedThenException() {
        when(commandLine.hasOption("keygenvaulttype")).thenReturn(false);
        when(commandLine.hasOption("keygenvaulturl")).thenReturn(true);
        when(commandLine.getOptionValue("keygenvaulturl")).thenReturn("someurl");

        Throwable ex = catchThrowable(() -> this.parser.parse(commandLine));

        assertThat(ex).isInstanceOf(CliException.class);
        assertThat(ex.getMessage()).isEqualTo("Key vault type either not provided or not recognised");
    }

    @Test
    public void ifVaultOptionsProvidedButTypeUnknownThenException() {
        when(commandLine.hasOption("keygenvaulttype")).thenReturn(true);
        when(commandLine.hasOption("keygenvaulturl")).thenReturn(true);
        when(commandLine.getOptionValue("keygenvaulttype")).thenReturn("unknown");

        Throwable ex = catchThrowable(() -> this.parser.parse(commandLine));

        assertThat(ex).isInstanceOf(CliException.class);
        assertThat(ex.getMessage()).isEqualTo("Key vault type either not provided or not recognised");
    }

    @Test
    public void noFilenameProvidedWhenUsingHashicorpVaultThrowsException() {
        when(commandLine.hasOption("keygenvaulttype")).thenReturn(true);
        when(commandLine.hasOption("keygenvaulturl")).thenReturn(true);
        when(commandLine.getOptionValue("keygenvaulttype")).thenReturn("HASHICORP");
        when(commandLine.hasOption("filename")).thenReturn(false);

        Throwable ex = catchThrowable(() -> this.parser.parse(commandLine));

        assertThat(ex).isInstanceOf(CliException.class);
        assertThat(ex.getMessage())
                .isEqualTo("At least one -filename must be provided when saving generated keys in a Hashicorp Vault");
    }

    @Test
    public void ifAllVaultOptionsAndFilenameProvidedForHashicorpThenOkay() throws Exception {
        when(commandLine.hasOption("keygenvaulttype")).thenReturn(true);
        when(commandLine.hasOption("keygenvaulturl")).thenReturn(true);
        when(commandLine.hasOption("filename")).thenReturn(true);
        when(commandLine.getOptionValue("keygenvaulturl")).thenReturn("someurl");
        when(commandLine.getOptionValue("keygenvaulttype")).thenReturn("HASHICORP");
        when(commandLine.getOptionValue("filename")).thenReturn("secret/path");
        when(commandLine.getOptionValue("keygenvaultapprole")).thenReturn("approle");
        when(commandLine.getOptionValue("keygenvaultsecretengine")).thenReturn("secretEngine");

        Path tempPath = Files.createTempFile(UUID.randomUUID().toString(), "");
        tempPath.toFile().deleteOnExit();

        when(commandLine.getOptionValue("keygenvaultkeystore")).thenReturn(tempPath.toString());
        when(commandLine.getOptionValue("keygenvaulttruststore")).thenReturn(tempPath.toString());

        this.parser.parse(commandLine);

        verify(commandLine, times(1)).getOptionValue("keygenvaulttype");
        verify(commandLine, times(1)).getOptionValue("keygenvaulturl");
        verify(commandLine, times(1)).getOptionValue("keygenvaultapprole");
        verify(commandLine, times(1)).getOptionValue("keygenvaultkeystore");
        verify(commandLine, times(1)).getOptionValue("keygenvaulttruststore");
        verify(commandLine, times(1)).getOptionValue("keygenvaultsecretengine");
    }

    @Test
    public void ifHashicorpTlsOptionsProvidedButPathsDontExistThenValidationException() {
        when(commandLine.hasOption("keygenvaulttype")).thenReturn(true);
        when(commandLine.hasOption("keygenvaulturl")).thenReturn(true);
        when(commandLine.getOptionValue("keygenvaulttype")).thenReturn("HASHICORP");
        when(commandLine.getOptionValue("keygenvaulturl")).thenReturn("someurl");
        when(commandLine.hasOption("filename")).thenReturn(true);
        when(commandLine.getOptionValue("filename")).thenReturn("secret/path");
        when(commandLine.getOptionValue("keygenvaultsecretengine")).thenReturn("secretEngine");
        when(commandLine.getOptionValue("keygenvaultapprole")).thenReturn("approle");
        when(commandLine.getOptionValue("keygenvaultkeystore")).thenReturn("non/existent/path");
        when(commandLine.getOptionValue("keygenvaulttruststore")).thenReturn("non/existent/path");

        Throwable ex = catchThrowable(() -> this.parser.parse(commandLine));

        verify(commandLine, times(1)).getOptionValue("keygenvaulttype");
        verify(commandLine, times(1)).getOptionValue("keygenvaulturl");
        verify(commandLine, times(1)).getOptionValue("keygenvaultapprole");
        verify(commandLine, times(1)).getOptionValue("keygenvaultkeystore");
        verify(commandLine, times(1)).getOptionValue("keygenvaulttruststore");
        verify(commandLine, times(1)).getOptionValue("keygenvaultsecretengine");

        assertThat(ex).isInstanceOf(ConstraintViolationException.class);

        Set<ConstraintViolation<?>> violations = ((ConstraintViolationException) ex).getConstraintViolations();

        assertThat(violations.size()).isEqualTo(2);

        Iterator<ConstraintViolation<?>> iterator = violations.iterator();

        assertThat(iterator.next().getMessage()).isEqualTo("File does not exist");
        assertThat(iterator.next().getMessage()).isEqualTo("File does not exist");
    }

    @Test
    public void lowercaseVaultTypeIsOkay() throws Exception {
        when(commandLine.hasOption("keygenvaulttype")).thenReturn(true);
        when(commandLine.hasOption("keygenvaulturl")).thenReturn(true);
        when(commandLine.hasOption("filename")).thenReturn(true);
        when(commandLine.getOptionValue("keygenvaulturl")).thenReturn("someurl");
        when(commandLine.getOptionValue("filename")).thenReturn("secret/path");
        when(commandLine.getOptionValue("keygenvaultapprole")).thenReturn("approle");
        when(commandLine.getOptionValue("keygenvaultsecretengine")).thenReturn("secretEngine");

        when(commandLine.getOptionValue("keygenvaulttype")).thenReturn("hashicorp");

        Path tempPath = Files.createTempFile(UUID.randomUUID().toString(), "");
        tempPath.toFile().deleteOnExit();

        when(commandLine.getOptionValue("keygenvaultkeystore")).thenReturn(tempPath.toString());
        when(commandLine.getOptionValue("keygenvaulttruststore")).thenReturn(tempPath.toString());

        this.parser.parse(commandLine);

        verify(commandLine, times(1)).getOptionValue("keygenvaulttype");
        verify(commandLine, times(1)).getOptionValue("keygenvaulturl");
        verify(commandLine, times(1)).getOptionValue("keygenvaultapprole");
        verify(commandLine, times(1)).getOptionValue("keygenvaultkeystore");
        verify(commandLine, times(1)).getOptionValue("keygenvaulttruststore");
        verify(commandLine, times(1)).getOptionValue("keygenvaultsecretengine");
    }

    @Test
    public void leadingWhitespaceVaultTypeIsOkay() throws Exception {
        when(commandLine.hasOption("keygenvaulttype")).thenReturn(true);
        when(commandLine.hasOption("keygenvaulturl")).thenReturn(true);
        when(commandLine.hasOption("filename")).thenReturn(true);
        when(commandLine.getOptionValue("keygenvaulturl")).thenReturn("someurl");
        when(commandLine.getOptionValue("filename")).thenReturn("secret/path");
        when(commandLine.getOptionValue("keygenvaultapprole")).thenReturn("approle");
        when(commandLine.getOptionValue("keygenvaultsecretengine")).thenReturn("secretEngine");

        when(commandLine.getOptionValue("keygenvaulttype")).thenReturn("  HASHICORP");

        Path tempPath = Files.createTempFile(UUID.randomUUID().toString(), "");
        tempPath.toFile().deleteOnExit();

        when(commandLine.getOptionValue("keygenvaultkeystore")).thenReturn(tempPath.toString());
        when(commandLine.getOptionValue("keygenvaulttruststore")).thenReturn(tempPath.toString());

        this.parser.parse(commandLine);

        verify(commandLine, times(1)).getOptionValue("keygenvaulttype");
        verify(commandLine, times(1)).getOptionValue("keygenvaulturl");
        verify(commandLine, times(1)).getOptionValue("keygenvaultapprole");
        verify(commandLine, times(1)).getOptionValue("keygenvaultkeystore");
        verify(commandLine, times(1)).getOptionValue("keygenvaulttruststore");
        verify(commandLine, times(1)).getOptionValue("keygenvaultsecretengine");
    }

    @Test
    public void trailingWhitespaceVaultTypeIsOkay() throws Exception {
        when(commandLine.hasOption("keygenvaulttype")).thenReturn(true);
        when(commandLine.hasOption("keygenvaulturl")).thenReturn(true);
        when(commandLine.hasOption("filename")).thenReturn(true);
        when(commandLine.getOptionValue("keygenvaulturl")).thenReturn("someurl");
        when(commandLine.getOptionValue("filename")).thenReturn("secret/path");
        when(commandLine.getOptionValue("keygenvaultapprole")).thenReturn("approle");
        when(commandLine.getOptionValue("keygenvaultsecretengine")).thenReturn("secretEngine");

        when(commandLine.getOptionValue("keygenvaulttype")).thenReturn("HASHICORP  ");

        Path tempPath = Files.createTempFile(UUID.randomUUID().toString(), "");
        tempPath.toFile().deleteOnExit();

        when(commandLine.getOptionValue("keygenvaultkeystore")).thenReturn(tempPath.toString());
        when(commandLine.getOptionValue("keygenvaulttruststore")).thenReturn(tempPath.toString());

        this.parser.parse(commandLine);

        verify(commandLine, times(1)).getOptionValue("keygenvaulttype");
        verify(commandLine, times(1)).getOptionValue("keygenvaulturl");
        verify(commandLine, times(1)).getOptionValue("keygenvaultapprole");
        verify(commandLine, times(1)).getOptionValue("keygenvaultkeystore");
        verify(commandLine, times(1)).getOptionValue("keygenvaulttruststore");
        verify(commandLine, times(1)).getOptionValue("keygenvaultsecretengine");
    }
}
