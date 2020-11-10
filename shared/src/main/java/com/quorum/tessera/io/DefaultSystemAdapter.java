package com.quorum.tessera.io;

import java.io.PrintStream;

public class DefaultSystemAdapter implements SystemAdapter {

    @Override
    public PrintStream out() {
        return System.out;
    }
    @Override
    public PrintStream err() {
        return System.err;
    }
}
