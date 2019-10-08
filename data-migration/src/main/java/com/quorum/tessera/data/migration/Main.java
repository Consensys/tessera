package com.quorum.tessera.data.migration;

import com.quorum.tessera.cli.CliDelegate;
import com.quorum.tessera.cli.CliResult;

import java.util.Arrays;

public class Main {

    private Main() {
        throw new UnsupportedOperationException("");
    }

    public static void main(final String... args) {

        try {
            final CliResult cliResult = CliDelegate.instance().execute(args);
            System.exit(cliResult.getStatus());
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
