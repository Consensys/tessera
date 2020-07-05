package com.quorum.tessera.io;

import com.quorum.tessera.loader.ServiceLoaderUtil;

import java.io.PrintStream;

public interface SystemAdapter {

    SystemAdapter INSTANCE = ServiceLoaderUtil.load(SystemAdapter.class).orElse(new SystemAdapter() {
    });

    default PrintStream out() {
        return System.out;
    }

    default PrintStream err() {
        return System.err;
    }

}
