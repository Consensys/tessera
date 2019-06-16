package com.quorum.tessera.io;

import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class SystemAdapterTest {

    private final SystemAdapter systemAdapter = SystemAdapter.INSTANCE;

    private PrintStream outPrintStream;

    private PrintStream errPrintStream;

    @Before
    public void onSetup() {

        assertThat(systemAdapter).isInstanceOf(MockSystemAdapter.class);

        outPrintStream = mock(PrintStream.class);
        errPrintStream = mock(PrintStream.class);

        MockSystemAdapter.class.cast(systemAdapter).setErrPrintStream(errPrintStream);

        MockSystemAdapter.class.cast(systemAdapter).setOutPrintStream(outPrintStream);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(outPrintStream, errPrintStream);
    }

    @Test
    public void outIsOut() {

        systemAdapter.out().print("Hellow");
        assertThat(systemAdapter.out()).isSameAs(outPrintStream);
        verify(outPrintStream).print("Hellow");
    }

    @Test
    public void errorIsErr() {

        systemAdapter.err().print("Hellow");
        assertThat(systemAdapter.err()).isSameAs(errPrintStream);
        verify(errPrintStream).print("Hellow");
    }

    @Test
    public void executeDefaultInstance() {
        SystemAdapter instance = new SystemAdapter() {
        };

        assertThat(instance.err()).isSameAs(System.err);
        assertThat(instance.out()).isSameAs(System.out);

    }

    @Test
    public void executeNoop() {
        NoopSystemAdapter instance = new NoopSystemAdapter();
        instance.out().print(this);
        assertThat(instance.err()).isNotSameAs(System.err);
        assertThat(instance.out()).isNotSameAs(System.out);
    }

}
