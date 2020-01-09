package com.quorum.tessera.passwords;

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
        if (this.inputStream.hasNextLine()) {
            return this.inputStream.nextLine();
        } else {
            return "";
        }
    }
}
