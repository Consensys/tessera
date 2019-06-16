package com.quorum.tessera.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class NoopSystemAdapter implements SystemAdapter {
    
    private static final NoopPrintStream PRINT_STREAM = new NoopPrintStream();
    
    @Override
    public PrintStream out() {
        return PRINT_STREAM;
    }

    @Override
    public PrintStream err() {
        return PRINT_STREAM;
    }

    static class NoopPrintStream extends PrintStream {

       NoopPrintStream() {
            super(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                }
            });
        }
    
    }
    
}
