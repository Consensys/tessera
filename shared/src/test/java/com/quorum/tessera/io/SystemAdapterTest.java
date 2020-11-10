package com.quorum.tessera.io;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;

import static org.assertj.core.api.Assertions.assertThat;

public class SystemAdapterTest {

    private final SystemAdapter systemAdapter = SystemAdapter.INSTANCE;

    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Rule
    public final SystemErrRule systemErrRule = new SystemErrRule().enableLog();


    @Before
    public void onSetup() {

        assertThat(systemAdapter).isInstanceOf(DefaultSystemAdapter.class);



    }

    @After
    public void onTearDown() {
    }

    @Test
    public void outIsOut() {
        systemAdapter.out().print("Hellow");
        assertThat(systemOutRule.getLog()).isEqualTo("Hellow");
    }

    @Test
    public void errorIsErr() {

        systemAdapter.err().print("Hellow");
        assertThat(systemErrRule.getLog()).isEqualTo("Hellow");
    }

    @Test
    public void executeDefaultInstance() {
        SystemAdapter instance = SystemAdapter.INSTANCE;

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
