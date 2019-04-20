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

/**
 * <p>
 *  Delegates calls to nio Files functions unchecking IOExceptions
 *  and providing a means of mocking file system interactions.
 * </p>
 * 
 * @see java.nio.file.Files
 */
public interface FilesDelegate {

    default boolean notExists(Path path, LinkOption... options) {
        return Files.notExists(path, options);
    }

    default boolean deleteIfExists(Path path) {
        return IOCallback.execute(() -> Files.deleteIfExists(path));
    }

    default Path createFile(Path path, FileAttribute... attributes) {
        return IOCallback.execute(() -> Files.createFile(path, attributes));
    }

    default InputStream newInputStream(Path path, OpenOption... options) {
        return IOCallback.execute(() -> Files.newInputStream(path, options));
    }

    default boolean exists(Path path, LinkOption... options) {
        return Files.exists(path, options);
    }

    default byte[] readAllBytes(Path path) {
        return IOCallback.execute(() -> Files.readAllBytes(path));
    }

    default Stream<String> lines(Path path) {
        return IOCallback.execute(() -> Files.lines(path));
    }

    default Path write(Path path, byte[] bytes, OpenOption... options) {
        return IOCallback.execute(() -> Files.write(path, bytes, options));
    }

    default Path setPosixFilePermissions(Path path, Set<PosixFilePermission> perms) {
        return IOCallback.execute(() -> Files.setPosixFilePermissions(path, perms));
    }

    static FilesDelegate create() {
        return ServiceLoaderUtil.load(FilesDelegate.class).orElse(new FilesDelegate() {
        });
    }

}
