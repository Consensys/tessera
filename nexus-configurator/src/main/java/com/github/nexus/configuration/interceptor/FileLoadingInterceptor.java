package com.github.nexus.configuration.interceptor;

import javax.el.ELContext;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * Uses Java EL to load files and replace their position in the configuration
 * to the file contents
 */
public class FileLoadingInterceptor implements ConfigurationInterceptor {

    private final Method method;
    

    public FileLoadingInterceptor() {
         method = execute(() -> FileLoadingInterceptor.class.getDeclaredMethod("loadFile", String.class));
    }

    public static String loadFile(final String filePath) throws IOException {

        final Path path = Paths.get(filePath);

        return new String(Files.readAllBytes(path), UTF_8);

    }

    @Override
    public void register(final ELContext eLContext) {
        eLContext
            .getFunctionMapper()
            .mapFunction("", "file", method);
    }
    
    protected static <T> T execute(Callback<T> callback) {
        try {
            return callback.execute();
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        } 
    }
    
    interface Callback<T> {
        T execute() throws NoSuchMethodException;
    }
    
}
