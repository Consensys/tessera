package com.quorum.tessera.config.constraints;

import com.quorum.tessera.config.SslAuthenticationMode;
import com.quorum.tessera.config.SslConfig;
import com.quorum.tessera.config.SslTrustMode;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.validation.ConstraintValidatorContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class SslConfigValidatorTest {

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    private Path tmpFile;

    @Mock
    private ValidSsl validSsl;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    private SslConfigValidator validator;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        doNothing().when(context).disableDefaultConstraintViolation();
        when(builder.addConstraintViolation()).thenReturn(context);
        when(context.buildConstraintViolationWithTemplate(any())).thenReturn(builder);
        tmpFile = Paths.get(tmpDir.getRoot().getPath(), "tmpFile");
        Files.createFile(tmpFile);
        validator = new SslConfigValidator();
        when(validSsl.checkSslValid()).thenReturn(true);
        validator.initialize(validSsl);
    }

    @Test
    public void testNoCheckValidSsl() {
        when(validSsl.checkSslValid()).thenReturn(false);
        validator.initialize(validSsl);
        SslConfig sslConfig = null;
        assertThat(validator.isValid(sslConfig, context)).isTrue();
    }

    @Test
    public void testSslConfigNull() {
        SslConfig sslConfig = null;
        assertThat(validator.isValid(sslConfig, context)).isTrue();
    }

    @Test
    public void testSslConfigNotNullButTlsOff() {
        SslConfig sslConfig = new SslConfig(
                SslAuthenticationMode.OFF, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isTrue();
    }

    @Test
    public void testTlsAllowKeyStoreGeneration() {
        SslConfig sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT, true, null, null, null, null, SslTrustMode.NONE, null, null, null, null, SslTrustMode.NONE, null, null, null, null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isTrue();
    }

    @Test
    public void testKeyStoreConfigInvalid() {
        SslConfig sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, Paths.get("somefile"), "somepassword", null, null, null, Paths.get("somefile"), null, null, null, null, null, null, null, null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, null, null, null, null, Paths.get("somefile"), null, null, null, null, null, null, null, null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", null, null, null, null, null, null, null, null, null, null, null, null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", null, null, null, Paths.get("somefile"), "password", null, null, null, null, null, null, null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", null, null, null, tmpFile, null, null, null, null, null, null, null, null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT, false, tmpFile, null, null, null, null, tmpFile, null, null, null, null, null, null, null, null,Paths.get("someFile"),null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT, false, tmpFile, null, null, null, null, tmpFile, null, null, null, null, null, null, null, null,Paths.get("someFile"),Paths.get("someFile"),null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT, false, tmpFile, null, null, null, null, tmpFile, null, null, null, null, null, null, null, null, tmpFile,Paths.get("someFile"),null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT, false, tmpFile, null, null, null, null, tmpFile, null, null, null, null, null, null, null, null, tmpFile, tmpFile, Paths.get("someFile"),null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT, false, tmpFile, null, null, null, null, tmpFile, null, null, null, null, null, null, null, null, tmpFile, tmpFile, Paths.get("someFile"),Paths.get("someFile")
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT, false, tmpFile, null, null, null, null, tmpFile, null, null, null, null, null, null, null, null, tmpFile, tmpFile, tmpFile, Paths.get("someFile")
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

    }

    @Test
    public void testTrustModeNull() {
        SslConfig sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", null, null, null, tmpFile, "password", null, null, null, null, null, null, null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", null, null, SslTrustMode.CA, tmpFile, "password", null, null, null, null, null, null, null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();
    }

    @Test
    public void testTrustModeWhiteListButKnownHostsFileNotExisted() {
        SslConfig sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", null, null, SslTrustMode.WHITELIST, tmpFile, "password", null, null, SslTrustMode.WHITELIST, null, null, null, null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", null, null, SslTrustMode.WHITELIST, tmpFile, "password", null, null, SslTrustMode.WHITELIST, Paths.get("somefile"), null, null, null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", null, null, SslTrustMode.WHITELIST, tmpFile, "password", null, null, SslTrustMode.WHITELIST, tmpFile, null, null, null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", null, null, SslTrustMode.WHITELIST, tmpFile, "password", null, null, SslTrustMode.WHITELIST, tmpFile, Paths.get("some"),null,null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();
    }

    @Test
    public void testTrustModeCAButTrustStoreConfigInvalid() {
        SslConfig sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", null, null, SslTrustMode.CA, tmpFile, "password", null, null, SslTrustMode.NONE, null, null, null, null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", tmpFile, null, SslTrustMode.CA, tmpFile, "password", null, null, SslTrustMode.NONE, null, null, null, null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", Paths.get("somefile"), "password", SslTrustMode.CA, tmpFile, "password", null, null, SslTrustMode.NONE, null, null, null, null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", tmpFile, "p", SslTrustMode.CA, tmpFile, "password", null, null, SslTrustMode.CA, null, null, null, null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", tmpFile, null, SslTrustMode.CA, tmpFile, "password", tmpFile, null, SslTrustMode.CA, null, null, null, null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
                SslAuthenticationMode.STRICT, false, tmpFile, "password", Paths.get("somefile"), "password", SslTrustMode.CA, tmpFile, "password", Paths.get("somefile"), "p", SslTrustMode.CA, null, null, null, null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();
    }

    @Test
    public void testNoKeyStoreFilesButPemFilesProvided() {
        SslConfig sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT, false, null, null, null, null, SslTrustMode.CA, null, null, null, null, SslTrustMode.CA, null, null, Arrays.asList(tmpFile), Arrays.asList(tmpFile), tmpFile,tmpFile,tmpFile,tmpFile
        );
        assertThat(validator.isValid(sslConfig, context)).isTrue();
    }

    @Test
    public void testValidSsl() {
        SslConfig sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT, false, tmpFile, "pw", tmpFile, "pw", SslTrustMode.CA, tmpFile, "pw", tmpFile, "pw", SslTrustMode.CA, tmpFile, tmpFile, Arrays.asList(tmpFile), Arrays.asList(tmpFile), tmpFile,tmpFile,tmpFile,tmpFile
        );
        assertThat(validator.isValid(sslConfig, context)).isTrue();
    }
}
