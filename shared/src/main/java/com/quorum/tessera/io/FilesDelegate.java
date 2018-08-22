package com.quorum.tessera.io;

import com.quorum.tessera.ServiceLoaderUtil;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import java.util.stream.Stream;

public interface FilesDelegate {

    /**
     * @see java.nio.file.Files#notExists(java.nio.file.Path,
     * java.nio.file.LinkOption...)
     */
    default boolean notExists(Path path, LinkOption... options) {
        return Files.notExists(path, options);
    }

    /**
     * @see java.nio.file.Files#deleteIfExists(java.nio.file.Path)
     */
    default boolean deleteIfExists(Path path) {
        return IOCallback.execute(() -> Files.deleteIfExists(path));
    }

    /**
     * @see java.nio.file.Files#createFile(java.nio.file.Path,
     * java.nio.file.attribute.FileAttribute...)
     */
    default Path createFile(Path path, FileAttribute... attributes) {

        return IOCallback.execute(() -> Files.createFile(path, attributes));

    }

    /**
     * @see java.nio.file.Files#newInputStream(java.nio.file.Path,
     * java.nio.file.OpenOption...)
     */
    default InputStream newInputStream(Path path, OpenOption... options) {
        return IOCallback.execute(() -> Files.newInputStream(path, options));
    }

    /**
     * @see java.nio.file.Files#exists(java.nio.file.Path,
     * java.nio.file.LinkOption...)
     */
    default boolean exists(Path path, LinkOption... options) {
        return Files.exists(path, options);
    }

    /**
     *
     * @see java.nio.file.Files#readAllBytes(java.nio.file.Path)
     */
    default byte[] readAllBytes(Path path) {
        return IOCallback.execute(() -> Files.readAllBytes(path));
    }

    /**
     *
     * @see java.nio.file.Files#lines
     */
    default Stream<String> lines(Path path) {
        return IOCallback.execute(() -> Files.lines(path));
    }

    /**
     *
     * @see java.nio.file.Files#write(java.nio.file.Path, byte...,
     * java.nio.file.OpenOption...)
     */
    default Path write(Path path, byte[] bytes, OpenOption... options) {
        return IOCallback.execute(() -> Files.write(path, bytes, options));
    }

    /**
     *
     * @see java.nio.file.Files#setPosixFilePermissions(java.nio.file.Path path,
            Set<PosixFilePermission> perms)
     */
    default Path setPosixFilePermissions(Path path,
            Set<PosixFilePermission> perms) {
        return IOCallback.execute(() -> Files.setPosixFilePermissions(path, perms));
    }

    static FilesDelegate create() {
        return ServiceLoaderUtil.load(FilesDelegate.class).orElse(new FilesDelegate() {
        });
    }

}
