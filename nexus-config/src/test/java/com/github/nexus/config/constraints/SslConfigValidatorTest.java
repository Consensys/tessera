package com.github.nexus.config.constraints;

import com.github.nexus.config.SslAuthenticationMode;
import com.github.nexus.config.SslConfig;
import com.github.nexus.config.SslTrustMode;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.validation.ConstraintValidatorContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class SslConfigValidatorTest {

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    private File tmpFile;

    private static TemporaryFolder delegate;

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
        tmpFile = new File(tmpDir.getRoot(), "tmpFile");
        tmpFile.createNewFile();
        validator = new SslConfigValidator();
        when(validSsl.checkSslValid()).thenReturn(true);
        validator.initialize(validSsl);
    }

    @After
    public void after() {
        delegate = tmpDir;
    }

    @AfterClass
    public static void tearDown() {
        assertThat(delegate.getRoot().exists()).isFalse();
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
            SslAuthenticationMode.OFF,false,null,null,null,null,null,null,null,null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isTrue();
    }

    @Test
    public void testKeyStoreConfigInValid() {
        SslConfig sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT,false, null,null,null,null,null,null,null,null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT,false, Paths.get("somefile"),"somepassword",null,null,null,Paths.get("somefile"),null,null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT,false, Paths.get(tmpFile.getPath()),null,null,null,null,Paths.get("somefile"),null,null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT,false, Paths.get(tmpFile.getPath()),"password",null,null,null, null,null,null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT,false, Paths.get(tmpFile.getPath()),"password",null,null,null, Paths.get("somefile"),"password",null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT,false, Paths.get(tmpFile.getPath()),"password",null,null,null, Paths.get(tmpFile.getPath()),null,null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

    }

    @Test
    public void testTrustModeNull() {
        SslConfig sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT,false, Paths.get(tmpFile.getPath()),"password",null,null,null,Paths.get(tmpFile.getPath()),"password",null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT,false, Paths.get(tmpFile.getPath()),"password",null,null,SslTrustMode.CA ,Paths.get(tmpFile.getPath()),"password",null,null,null,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();
    }

    @Test
    public void testTrustModeWhiteListButKnownHostsFileNotExisted() {
        SslConfig sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT,false, Paths.get(tmpFile.getPath()),"password",null,null,SslTrustMode.WHITELIST ,Paths.get(tmpFile.getPath()),"password",null,null,SslTrustMode.WHITELIST,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT,false, Paths.get(tmpFile.getPath()),"password",null,null,SslTrustMode.WHITELIST ,Paths.get(tmpFile.getPath()),"password",null,null,SslTrustMode.WHITELIST,Paths.get("somefile"),null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT,false, Paths.get(tmpFile.getPath()),"password",null,null,SslTrustMode.WHITELIST ,Paths.get(tmpFile.getPath()),"password",null,null,SslTrustMode.WHITELIST,Paths.get(tmpFile.getPath()),null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT,false, Paths.get(tmpFile.getPath()),"password",null,null,SslTrustMode.WHITELIST ,Paths.get(tmpFile.getPath()),"password",null,null,SslTrustMode.WHITELIST,Paths.get(tmpFile.getPath()),Paths.get("some")
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();
    }

    @Test
    public void testTrustModeCAButTrustStoreConfigInValid() {
        SslConfig sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT,false, Paths.get(tmpFile.getPath()),"password",null,null,SslTrustMode.CA ,Paths.get(tmpFile.getPath()),"password",null,null,SslTrustMode.NONE,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT,false, Paths.get(tmpFile.getPath()),"password",Paths.get(tmpFile.getPath()),null,SslTrustMode.CA ,Paths.get(tmpFile.getPath()),"password",null,null,SslTrustMode.NONE,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT,false, Paths.get(tmpFile.getPath()),"password",Paths.get("somefile"),"password",SslTrustMode.CA ,Paths.get(tmpFile.getPath()),"password",null,null,SslTrustMode.NONE,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT,false, Paths.get(tmpFile.getPath()),"password",Paths.get(tmpFile.getPath()),"p",SslTrustMode.NONE ,Paths.get(tmpFile.getPath()),"password",null,null,SslTrustMode.CA,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT,false, Paths.get(tmpFile.getPath()),"password",Paths.get(tmpFile.getPath()),null,SslTrustMode.NONE ,Paths.get(tmpFile.getPath()),"password",Paths.get(tmpFile.getPath()),null,SslTrustMode.CA,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();

        sslConfig = new SslConfig(
            SslAuthenticationMode.STRICT,false, Paths.get(tmpFile.getPath()),"password",Paths.get("somefile"),"password",SslTrustMode.NONE ,Paths.get(tmpFile.getPath()),"password",Paths.get("somefile"),"p",SslTrustMode.CA,null,null
        );
        assertThat(validator.isValid(sslConfig, context)).isFalse();
    }
}
