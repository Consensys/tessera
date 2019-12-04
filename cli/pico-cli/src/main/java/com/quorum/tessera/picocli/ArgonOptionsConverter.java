package com.quorum.tessera.picocli;

import com.quorum.tessera.config.ArgonOptions;
import com.quorum.tessera.config.util.JaxbUtil;
import picocli.CommandLine;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ArgonOptionsConverter implements CommandLine.ITypeConverter<ArgonOptions> {

    @Override
    public ArgonOptions convert(String value) throws Exception {
        final Path path = Paths.get(value);

        if (!Files.exists(path)) {
            throw new FileNotFoundException(String.format("%s not found.", path));
        }

        try (InputStream in = Files.newInputStream(path)) {
            return JaxbUtil.unmarshal(in, ArgonOptions.class);
        }
    }
}
