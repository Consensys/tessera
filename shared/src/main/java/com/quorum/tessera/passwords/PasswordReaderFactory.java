package com.quorum.tessera.passwords;

import java.util.Optional;

public class PasswordReaderFactory {

  public static PasswordReader create() {
    return Optional.ofNullable(System.console())
        .map(ConsolePasswordReader::new)
        .map(PasswordReader.class::cast)
        .orElseGet(() -> new InputStreamPasswordReader(System.in));
  }
}
