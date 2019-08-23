package com.quorum.tessera.nacl;

import com.quorum.tessera.ServiceLoaderUtil;

/** A factory for providing the implementation of the {@link NaclFacade} with all its dependencies set up */
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
     * @return the factory implementation that will provide instances of that implementations {@link NaclFacade}
     */
    static NaclFacadeFactory newFactory() {
        // TODO: return the stream and let the caller deal with it
        return ServiceLoaderUtil.loadAll(NaclFacadeFactory.class).findAny().get();
    }
}
