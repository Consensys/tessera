
package com.quorum.tessera.jaxrs;

import com.quorum.tessera.service.locator.ServiceLocator;
import java.util.Collections;
import java.util.Set;


public class MockServiceLocator implements ServiceLocator{

    @Override
    public Set<Object> getServices(String filename) {
        return Collections.emptySet();
    }

}
