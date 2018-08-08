package com.quorum.tessera.config.util;

import java.io.Console;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Allows a password to be read from the System console if it is available
 * otherwise reads from the provided input stream
 */
public class PasswordReader {

    private final Console console;

    private final Scanner systemIn;

    public PasswordReader(final Console console, final InputStream systemIn) {
        this.console = console;
        this.systemIn = new Scanner(systemIn);
    }

    /**
     * Read a password from either system console or the given stream
     * @return the read password, which may be empty if no password is given
     */
    public String readPassword() {
        if(this.console == null) {
            return this.systemIn.nextLine();
        }

        final char[] consolePassword = this.console.readPassword();
        return new String(consolePassword);
    }

}
