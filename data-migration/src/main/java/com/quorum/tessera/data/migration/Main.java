package com.quorum.tessera.data.migration;

import java.util.Arrays;

public class Main {

    private Main() {
        throw new UnsupportedOperationException("");
    }

    public static void main(final String... args) {

        try {
            final int result = new CmdLineExecutor().execute(args);
            System.exit(result);
        } catch (final Exception ex) {
            System.err.println("An error has occurred: " + ex.getMessage());

            if (Arrays.asList(args).contains("debug")) {
                System.err.println();
                System.err.println("Exception message: " + ex.getMessage());
                System.err.println("Exception class: " + ex.getClass());
            }

            System.exit(1);
        }
    }
}
