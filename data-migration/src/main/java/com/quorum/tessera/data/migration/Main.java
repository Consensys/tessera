package com.quorum.tessera.data.migration;

public class Main {
    
    private Main() {
        throw new UnsupportedOperationException("");
    }
    
    public static void main(final String... args) {

        try {
            final int result = new CmdLineExecutor().execute(args);
            System.exit(result);
        } catch (final Exception ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }

    }
    
}
