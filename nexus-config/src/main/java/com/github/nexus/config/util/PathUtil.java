package com.github.nexus.config.util;

import com.github.nexus.config.ConfigException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Collectors;

public interface PathUtil {

    static String readData(Path path, String value) {

        try {
            if (Objects.nonNull(path) && Objects.isNull(value)) {
                return Files.lines(path)
                        .collect(Collectors.joining());
            }
            return value;
        } catch (IOException ex) {
            throw new ConfigException(ex);
        }
    }
    


}
