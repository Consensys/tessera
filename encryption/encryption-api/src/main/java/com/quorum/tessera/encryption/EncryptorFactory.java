package com.quorum.tessera.encryption;

import com.quorum.tessera.ServiceLoaderUtil;
import java.util.Arrays;
import java.util.List;

/** * A factory for providing the implementation of the {@link Encryptor} with all its dependencies set up */
public interface EncryptorFactory {

    /**
     * Retrieves a preconfigured NaclFacade
     *
     * @return the implementation of the {@link Encryptor}
     */
    Encryptor create();

    /**
     * Retrieves the implementation of the factory from the service loader
     *
     * @return the factory implementation that will provide instances of that implementations {@link Encryptor}
     */
    static EncryptorFactory newFactory() {
        // TODO: return the stream and let the caller deal with it
        List<String> naclDefaults = Arrays.asList("KaliumFactory", "JnaclFactory");

        return ServiceLoaderUtil.loadAll(EncryptorFactory.class)
                .filter(f -> naclDefaults.contains(f.getClass().getSimpleName()))
                .findAny()
                .orElse(ServiceLoaderUtil.load(EncryptorFactory.class).get());
    }
}
