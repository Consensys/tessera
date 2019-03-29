package com.quorum.tessera.config.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Converts a String to a Path object for JAXB de/serialisation
 */
public class PathAdapter extends XmlAdapter<String,Path> {

    @Override
    public Path unmarshal(final String input) {

        return Optional.ofNullable(input)
            .map(Paths::get)
            .filter(Files::exists)
            .orElse(null);
    }

    @Override
    public String marshal(final Path input) {
        return Optional.ofNullable(input)
            .map(Path::toAbsolutePath)
            .map(Path::toString).orElse(null);
    }

}
