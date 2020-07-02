package com.quorum.tessera.service.locator;

import com.quorum.tessera.ServiceLoaderUtil;
import java.util.Set;

/** Creates a set of services that are configured */
@Deprecated
public interface ServiceLocator {

    /**
     * Retrieves all the services specified in the configuration file
     *
     * @return the set of all configuration services
     */
    Set<Object> getServices();

    /**
     * Returns an implementation of the {@link ServiceLocator} from the service loader
     *
     * @return the {@link ServiceLocator} instance
     */
    static ServiceLocator create() {
        // TODO: return the stream and let the caller deal with it
        return ServiceLoaderUtil.loadAll(ServiceLocator.class)
                .filter(l -> l.getClass().isAnnotationPresent(Default.class))
                .findAny()
                .orElse(ServiceLoaderUtil.loadAll(ServiceLocator.class).findAny().get());
    }
}
