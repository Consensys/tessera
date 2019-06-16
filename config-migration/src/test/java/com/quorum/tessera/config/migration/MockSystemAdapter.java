package com.quorum.tessera.config.migration;

import com.quorum.tessera.io.NoopSystemAdapter;
import com.quorum.tessera.io.SystemAdapter;

import java.io.PrintStream;

public class MockSystemAdapter implements SystemAdapter {

    private PrintStream outPrintStream;
    
    private PrintStream errPrintStream;

    public MockSystemAdapter(NoopSystemAdapter defualtInstance) {
        this(defualtInstance.out(),defualtInstance.err());
    }

    public MockSystemAdapter() {
        this(new NoopSystemAdapter());
    }

    public MockSystemAdapter(PrintStream outPrintStream, PrintStream errPrintStream) {
        this.outPrintStream = outPrintStream;
        this.errPrintStream = errPrintStream;
    }

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
