package com.quorum.tessera.passwords;

import java.io.Console;

public class ConsolePasswordReader implements PasswordReader {

  private final Console console;

  public ConsolePasswordReader(final Console console) {
    this.console = console;
  }

  @Override
  public char[] readPasswordFromConsole() {
    return this.console.readPassword();
  }
}
