package com.quorum.tessera.config.adapters;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Converts a String to a Path object for JAXB de/serialisation */
public class PathAdapter extends XmlAdapter<String, Path> {

  @Override
  public Path unmarshal(final String input) {
    return (input == null) ? null : Paths.get(input);
  }

  @Override
  public String marshal(final Path input) {
    return (input == null) ? null : input.toAbsolutePath().toString();
  }
}
