package com.quorum.tessera.config.util;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigException;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.io.FilesDelegate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.APPEND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PasswordFileUpdaterWriterTest {
    private FilesDelegate filesDelegate;

    private PasswordFileUpdaterWriter writer;

    @Before
    public void setUp() {
        this.filesDelegate = mock(FilesDelegate.class);
        this.writer = new PasswordFileUpdaterWriter(filesDelegate);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(filesDelegate);
    }

    @Test
    public void configContainsPasswordListThrowsException() {
        Config config = mock(Config.class);
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);

        when(config.getKeys()).thenReturn(keyConfiguration);
        when(keyConfiguration.getPasswords()).thenReturn(Collections.singletonList("pwd"));

        Throwable ex = catchThrowable(() -> writer.updateAndWrite(null, config, null));

        assertThat(ex).isExactlyInstanceOf(ConfigException.class);
        assertThat(ex.getMessage()).contains("Configfile must contain \"passwordFile\" field. The \"passwords\" field is no longer supported.");
    }

    @Test
    public void passwordFileAlreadyExists() {
        Config config = mock(Config.class);
        Path pwdFile = mock(Path.class);
        String path = "somepath";
        when(pwdFile.toString()).thenReturn(path);

        when(filesDelegate.exists(pwdFile)).thenReturn(true);

        Throwable ex = catchThrowable(() -> writer.updateAndWrite(null, config, pwdFile));

        assertThat(ex).isExactlyInstanceOf(FileAlreadyExistsException.class);
        assertThat(ex.getMessage()).contains(path);

        verify(filesDelegate).exists(pwdFile);
    }

    @Test
    public void newPasswordsAppendedToExistingPasswordsAndWrittenToFile() throws Exception {
        Config config = mock(Config.class);
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        Path existingPwdFile = mock(Path.class);
        when(config.getKeys()).thenReturn(keyConfiguration);
        when(keyConfiguration.getPasswordFile()).thenReturn(existingPwdFile);

        Path pwdFile = mock(Path.class);
        String path = "somepath";
        when(pwdFile.toString()).thenReturn(path);

        List<String> existingPasswords = new ArrayList<>(Arrays.asList("pwd1", "pwd2"));

        ConfigKeyPair key1 = mock(ConfigKeyPair.class);
        when(key1.getPassword()).thenReturn("pwd3");

        ConfigKeyPair key2 = mock(ConfigKeyPair.class);
        when(key2.getPassword()).thenReturn("pwd4");

        List<String> existingAndNewPasswords = new ArrayList<>(Arrays.asList("pwd1", "pwd2", "pwd3", "pwd4"));

        List<ConfigKeyPair> newKeys = new ArrayList<>(Arrays.asList(key1, key2));

        when(filesDelegate.readAllLines(any())).thenReturn(existingPasswords);

        writer.updateAndWrite(newKeys, config, pwdFile);

        verify(filesDelegate).readAllLines(existingPwdFile);
        verify(filesDelegate).exists(pwdFile);
        verify(filesDelegate).createFile(pwdFile);
        verify(filesDelegate).setPosixFilePermissions(pwdFile, Stream.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE).collect(Collectors.toSet()));
        verify(filesDelegate).write(pwdFile, existingAndNewPasswords, APPEND);
    }

    @Test
    public void newPasswordsWrittenToNewFile() throws Exception {
        Config config = mock(Config.class);

        Path pwdFile = mock(Path.class);
        String path = "somepath";
        when(pwdFile.toString()).thenReturn(path);

        ConfigKeyPair key1 = mock(ConfigKeyPair.class);
        when(key1.getPassword()).thenReturn("pwd1");

        ConfigKeyPair key2 = mock(ConfigKeyPair.class);
        when(key2.getPassword()).thenReturn("pwd2");

        List<String> newPasswords = new ArrayList<>(Arrays.asList("pwd1", "pwd2"));

        List<ConfigKeyPair> newKeys = new ArrayList<>(Arrays.asList(key1, key2));

        writer.updateAndWrite(newKeys, config, pwdFile);

        verify(filesDelegate).exists(pwdFile);
        verify(filesDelegate).createFile(pwdFile);
        verify(filesDelegate).setPosixFilePermissions(pwdFile, Stream.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE).collect(Collectors.toSet()));
        verify(filesDelegate).write(pwdFile, newPasswords, APPEND);
    }

    @Test
    public void newPasswordsWrittenToNewFileIncludingEmptyLinesForExistingKeys() throws Exception {
        Config config = mock(Config.class);
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        List<ConfigKeyPair> existingKeys = Arrays.asList(mock(ConfigKeyPair.class), mock(ConfigKeyPair.class));

        when(config.getKeys()).thenReturn(keyConfiguration);
        when(keyConfiguration.getKeyData()).thenReturn(existingKeys);

        Path pwdFile = mock(Path.class);
        String path = "somepath";
        when(pwdFile.toString()).thenReturn(path);

        ConfigKeyPair key1 = mock(ConfigKeyPair.class);
        when(key1.getPassword()).thenReturn("pwd1");

        ConfigKeyPair key2 = mock(ConfigKeyPair.class);
        when(key2.getPassword()).thenReturn("pwd2");

        List<String> existingAndNewPasswords = new ArrayList<>(Arrays.asList("", "", "pwd1", "pwd2"));

        List<ConfigKeyPair> newKeys = new ArrayList<>(Arrays.asList(key1, key2));

        writer.updateAndWrite(newKeys, config, pwdFile);

        verify(filesDelegate).exists(pwdFile);
        verify(filesDelegate).createFile(pwdFile);
        verify(filesDelegate).setPosixFilePermissions(pwdFile, Stream.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE).collect(Collectors.toSet()));
        verify(filesDelegate).write(pwdFile, existingAndNewPasswords, APPEND);
    }
}
