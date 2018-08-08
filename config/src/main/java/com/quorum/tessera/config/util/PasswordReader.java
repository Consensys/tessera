package com.quorum.tessera.config.util;

import java.io.Console;
import java.io.InputStream;
import java.util.Scanner;

public class PasswordReader {

    private final Console console;

    private final InputStream systemIn;

    public PasswordReader(final Console console, final InputStream systemIn) {
        this.console = console;
        this.systemIn = systemIn;
    }

    public String readPassword() {
        if(this.console == null) {
            return new Scanner(this.systemIn).nextLine();
        }

        final char[] consolePassword = this.console.readPassword();
        return new String(consolePassword);
    }

}
