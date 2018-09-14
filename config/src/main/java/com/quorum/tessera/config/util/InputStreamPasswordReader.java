package com.quorum.tessera.config.util;

import java.io.InputStream;
import java.util.Scanner;

import static java.util.Objects.requireNonNull;

public class InputStreamPasswordReader implements PasswordReader {

    private final Scanner inputStream;

    public InputStreamPasswordReader(final InputStream inputStream) {
        this.inputStream = new Scanner(requireNonNull(inputStream));
    }

    @Override
    public String readPasswordFromConsole() {
        return this.inputStream.nextLine();
    }

}
