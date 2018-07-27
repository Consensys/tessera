package com.quorum.tessera.data.migration;

public class Main {
    
    private Main() {
        throw new UnsupportedOperationException("");
    }
    
    public static void main(String... args) throws Exception {
        try {
            System.exit(CmdLineExecutor.execute(args));
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }
    
}
