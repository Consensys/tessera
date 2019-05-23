package com.quorum.tessera.test.ssl.sample;

import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.Set;

public class SampleApplication extends Application {

    @Override
    public Set<Object> getSingletons() {
        return Collections.singleton(new SampleResource());
    }

}
