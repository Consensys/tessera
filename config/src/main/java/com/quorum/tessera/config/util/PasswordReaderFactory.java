package com.quorum.tessera.config.util;

import java.io.Console;

public class PasswordReaderFactory {

    public static PasswordReader create() {

        final Console console = System.console();

        if(console == null) {
            return new InputStreamPasswordReader(System.in);
        } else {
            return new ConsolePasswordReader(console);
        }

    }

}
