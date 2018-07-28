package com.quorum.tessera.nacl;

import java.util.ServiceLoader;

/**
 * A factory for providing the implementation of the {@link NaclFacade}
 * with all its dependencies set up
 */
public interface NaclFacadeFactory {

    /**
     * Retrieves a preconfigured NaclFacade
     *
     * @return the implementation of the {@link NaclFacade}
     */
    NaclFacade create();

    /**
     * Retrieves the implementation of the factory from the service loader
     *
     * @return the factory implementation that will provide instances of that
     * implementations {@link NaclFacade}
     */
    static NaclFacadeFactory newFactory() {
        return ServiceLoader.load(NaclFacadeFactory.class).iterator().next();
    }

}
