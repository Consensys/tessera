package com.quorum.tessera.config.util;

import static java.nio.file.StandardOpenOption.APPEND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigException;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.io.FilesDelegate;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
    final Config config = mock(Config.class);
    final KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);

    when(config.getKeys()).thenReturn(keyConfiguration);
    when(keyConfiguration.getPasswords()).thenReturn(Collections.singletonList("pwd"));

    final Throwable ex = catchThrowable(() -> writer.updateAndWrite(null, config, null));

    assertThat(ex).isExactlyInstanceOf(ConfigException.class);
    assertThat(ex.getMessage())
        .contains(
            "Configfile must contain \"passwordFile\" field. The \"passwords\" field is no longer supported.");
  }

  @Test
  public void passwordFileAlreadyExists() {
    final Config config = mock(Config.class);
    final Path pwdFile = mock(Path.class);
    final String path = "somepath";
    when(pwdFile.toString()).thenReturn(path);

    when(filesDelegate.exists(pwdFile)).thenReturn(true);

    final Throwable ex = catchThrowable(() -> writer.updateAndWrite(null, config, pwdFile));

    assertThat(ex).isExactlyInstanceOf(FileAlreadyExistsException.class);
    assertThat(ex.getMessage()).contains(path);

    verify(filesDelegate).exists(pwdFile);
  }

  @Test
  public void newPasswordsAppendedToExistingPasswordsAndWrittenToFile() throws Exception {
    final Config config = mock(Config.class);
    final KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    final Path existingPwdFile = mock(Path.class);
    when(config.getKeys()).thenReturn(keyConfiguration);
    when(keyConfiguration.getPasswordFile()).thenReturn(existingPwdFile);

    final Path pwdFile = mock(Path.class);
    final String path = "somepath";
    when(pwdFile.toString()).thenReturn(path);

    final List<String> existingPasswords = new ArrayList<>(Arrays.asList("pwd1", "pwd2"));

    final List<String> existingAndNewPasswords =
        new ArrayList<>(Arrays.asList("pwd1", "pwd2", "pwd3", "pwd4"));
    final List<char[]> newPasswords =
        new ArrayList<>(Arrays.asList("pwd3".toCharArray(), "pwd4".toCharArray()));

    when(filesDelegate.readAllLines(any())).thenReturn(existingPasswords);

    writer.updateAndWrite(newPasswords, config, pwdFile);

    verify(filesDelegate).readAllLines(existingPwdFile);
    verify(filesDelegate).exists(pwdFile);
    verify(filesDelegate).createFile(pwdFile);
    verify(filesDelegate)
        .setPosixFilePermissions(
            pwdFile,
            Stream.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)
                .collect(Collectors.toSet()));
    verify(filesDelegate).write(pwdFile, existingAndNewPasswords, APPEND);
  }

  @Test
  public void newPasswordsWrittenToNewFile() throws Exception {
    final Config config = mock(Config.class);

    final Path pwdFile = mock(Path.class);
    final String path = "somepath";
    when(pwdFile.toString()).thenReturn(path);

    final List<char[]> newPasswords =
        new ArrayList<>(Arrays.asList("pwd1".toCharArray(), "pwd2".toCharArray()));
    final List<String> newPasswordsStr = new ArrayList<>(Arrays.asList("pwd1", "pwd2"));

    writer.updateAndWrite(newPasswords, config, pwdFile);

    verify(filesDelegate).exists(pwdFile);
    verify(filesDelegate).createFile(pwdFile);
    verify(filesDelegate)
        .setPosixFilePermissions(
            pwdFile,
            Stream.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)
                .collect(Collectors.toSet()));
    verify(filesDelegate).write(pwdFile, newPasswordsStr, APPEND);
  }

  @Test
  public void newPasswordsWrittenToNewFileIncludingEmptyLinesForExistingKeys() throws Exception {
    final Config config = mock(Config.class);
    final KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    final List<KeyData> existingKeys = Arrays.asList(mock(KeyData.class), mock(KeyData.class));

    when(config.getKeys()).thenReturn(keyConfiguration);
    when(keyConfiguration.getKeyData()).thenReturn(existingKeys);

    final Path pwdFile = mock(Path.class);
    final String path = "somepath";
    when(pwdFile.toString()).thenReturn(path);

    final List<String> existingAndNewPasswords =
        new ArrayList<>(Arrays.asList("", "", "pwd1", "pwd2"));
    final List<char[]> newPasswords =
        new ArrayList<>(Arrays.asList("pwd1".toCharArray(), "pwd2".toCharArray()));

    writer.updateAndWrite(newPasswords, config, pwdFile);

    verify(filesDelegate).exists(pwdFile);
    verify(filesDelegate).createFile(pwdFile);
    verify(filesDelegate)
        .setPosixFilePermissions(
            pwdFile,
            Stream.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)
                .collect(Collectors.toSet()));
    verify(filesDelegate).write(pwdFile, existingAndNewPasswords, APPEND);
  }
}
