package com.quorum.tessera.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;

public class FilesDelegateTest {

    private FilesDelegate filesDelegate;

    @Before
    public void onSetUp() {
        filesDelegate = FilesDelegate.create();
    }

    @Test
    public void notExists() throws Exception {
        Path existentFile = Files.createTempFile(UUID.randomUUID().toString(), ".txt");
        existentFile.toFile().deleteOnExit();
        Path nonExistentFile = Paths.get(UUID.randomUUID().toString());

        assertThat(filesDelegate.notExists(existentFile)).isFalse();
        assertThat(filesDelegate.notExists(nonExistentFile)).isTrue();

    }

    @Test
    public void deleteIfExists() throws Exception {
        Path existentFile = Files.createTempFile(UUID.randomUUID().toString(), ".txt");
        existentFile.toFile().deleteOnExit();
        Path nonExistentFile = Paths.get(UUID.randomUUID().toString());

        assertThat(filesDelegate.deleteIfExists(existentFile)).isTrue();
        assertThat(filesDelegate.deleteIfExists(nonExistentFile)).isFalse();
    }

    @Test
    public void createFile() throws Exception {
        Path toBeCreated = Paths.get(UUID.randomUUID().toString());

        Path result = filesDelegate.createFile(toBeCreated);
        result.toFile().deleteOnExit();
        assertThat(toBeCreated).exists().isEqualTo(result);

    }

    @Test
    public void newInputStream() throws Exception {

        Path file = Files.createTempFile(UUID.randomUUID().toString(), ".txt");
        file.toFile().deleteOnExit();

        InputStream result = filesDelegate.newInputStream(file);

        assertThat(result).isNotNull();
    }

    @Test
    public void readAllBytes() throws Exception {
        byte[] someBytes = UUID.randomUUID().toString().getBytes();
        Path file = Files.createTempFile(UUID.randomUUID().toString(), ".txt");
        file.toFile().deleteOnExit();
        Files.write(file, someBytes);

        byte[] result = filesDelegate.readAllBytes(file);

        assertThat(result).isEqualTo(someBytes);

    }

    @Test
    public void exists() throws Exception {
        Path existentFile = Files.createTempFile(UUID.randomUUID().toString(), ".txt");
        existentFile.toFile().deleteOnExit();
        Path nonExistentFile = Paths.get(UUID.randomUUID().toString());

        assertThat(filesDelegate.exists(existentFile)).isTrue();
        assertThat(filesDelegate.exists(nonExistentFile)).isFalse();

    }

    @Test
    public void lines() throws Exception {
        Path somefile = Files.createTempFile("FilesDelegateTest#lines", ".txt");
        somefile.toFile().deleteOnExit();
        try (BufferedWriter writer = Files.newBufferedWriter(somefile)) {
            writer.write("ONE");
            writer.newLine();
            writer.write("");
            writer.newLine();
            writer.write("THREE");
        }

        List<String> results = filesDelegate.lines(somefile).collect(Collectors.toList());
        assertThat(results).containsExactly("ONE", "", "THREE");
    }

    @Test
    public void write() throws Exception {
        Path somefile = Paths.get("writeBytesTest");
        somefile.toFile().deleteOnExit();
        byte[] somebytes = UUID.randomUUID().toString().getBytes();

        Path result = filesDelegate.write(somefile, somebytes, StandardOpenOption.CREATE_NEW);
        assertThat(result).exists();
        assertThat(Files.readAllBytes(result)).isEqualTo(somebytes);

    }

    @Test
    public void setPosixFilePermissions() throws IOException {
        Path somefile = Files.createTempFile("setPosixFilePermissions", ".txt");
        somefile.toFile().deleteOnExit();
        Set<PosixFilePermission> perms = Stream.of(PosixFilePermission.values())
                .collect(Collectors.toSet());

        Path result = filesDelegate.setPosixFilePermissions(somefile, perms);
        assertThat(Files.getPosixFilePermissions(result)).containsAll(perms);
        

    }

}
