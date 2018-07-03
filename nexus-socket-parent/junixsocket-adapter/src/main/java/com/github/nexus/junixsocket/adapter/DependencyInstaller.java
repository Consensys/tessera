package com.github.nexus.junixsocket.adapter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class DependencyInstaller {

    private static final Path INSTALL_DIRECTORY = Paths.get("/tmp");

    private static final String SOCKET_SYS_PROP = "org.newsclub.net.unix.library.path";

    private static final String[] FILES = new String[]{
        "libjunixsocket-linux-1.5-amd64.so",
        "libjunixsocket-linux-1.5-i386.so",
        "libjunixsocket-macosx-1.5-i386.dylib",
        "libjunixsocket-macosx-1.5-x86_64.dylib"
    };

    public void installDependencies() {
//        final String presetDirectory = System.getProperty(SOCKET_SYS_PROP, INSTALL_DIRECTORY.toString());
//        System.setProperty(SOCKET_SYS_PROP, presetDirectory);
//
//        Stream.of(FILES).forEach(file -> this.copy(file, presetDirectory));
    }

    public void copy(final String name, final String installPath) {
        final Path filepath = Paths.get(installPath, name);

        if(Files.exists(filepath)) {
            return;
        }

        final InputStream stream = ClassLoader.getSystemResourceAsStream(name);

        try {
            final FileOutputStream fos = new FileOutputStream(filepath.toFile());

            int i;
            while ((i = stream.read()) != -1) {
                fos.write(i);
            }

            stream.close();
            fos.close();
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }


}
