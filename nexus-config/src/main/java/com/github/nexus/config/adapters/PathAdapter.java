package com.github.nexus.config.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Converts a String to a Path object for JAXB de/serialisation
 */
public class PathAdapter extends XmlAdapter<String,Path> {

    @Override
    public Path unmarshal(final String input) {
        if(Objects.isNull(input)) {
            return null;
        }

        return Paths.get(input);
    }

    @Override
    public String marshal(final Path input) {
        if(Objects.isNull(input)) {
            return null;
        }

        return input.toAbsolutePath().toString();
    }

}
