package com.quorum.tessera.io;

import java.io.PrintStream;

public class MockSystemAdapter implements SystemAdapter {
    
    private PrintStream outPrintStream;
    
    private PrintStream errPrintStream;

    public void setOutPrintStream(PrintStream outPrintStream) {
        this.outPrintStream = outPrintStream;
    }

    public void setErrPrintStream(PrintStream errPrintStream) {
        this.errPrintStream = errPrintStream;
    }
    
    @Override
    public PrintStream out() {
        return outPrintStream;
    }

    @Override
    public PrintStream err() {
        return errPrintStream;
    }
    
}
