package com.quorum.tessera.passwords;

import java.io.Console;

public class ConsolePasswordReader implements PasswordReader {

    private final Console console;

    public ConsolePasswordReader(final Console console) {
        this.console = console;
    }

    @Override
    public String readPasswordFromConsole() {
        final char[] consolePassword = this.console.readPassword();
        return new String(consolePassword);
    }
}
