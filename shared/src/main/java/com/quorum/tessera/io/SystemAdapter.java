package com.quorum.tessera.io;

import java.io.PrintStream;
import java.util.ServiceLoader;

public interface SystemAdapter {

    SystemAdapter INSTANCE = ServiceLoader.load(SystemAdapter.class).findFirst().get();

    PrintStream out();

    PrintStream err();

}
