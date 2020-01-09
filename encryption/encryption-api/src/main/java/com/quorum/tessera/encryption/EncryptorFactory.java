package com.quorum.tessera.encryption;

import com.quorum.tessera.ServiceLoaderUtil;
import java.util.Collections;
import java.util.Map;

/** * A factory for providing the implementation of the {@link Encryptor} with all its dependencies set up */
public interface EncryptorFactory {

    /**
     * Retrieves a preconfigured Encryptor
     *
     * @return the implementation of the {@link Encryptor}
     */
    default Encryptor create() {
        return create(Collections.emptyMap());
    }

    Encryptor create(Map<String, String> properties);

    String getType();

    /**
     * Retrieves the implementation of the factory from the service loader
     *
     * @return the factory implementation that will provide instances of that implementations {@link Encryptor}
     */
    static EncryptorFactory newFactory(String type) {
        return ServiceLoaderUtil.loadAll(EncryptorFactory.class)
                .filter(f -> f.getType().equals(type))
                .findAny()
                .orElse(ServiceLoaderUtil.load(EncryptorFactory.class).get());
    }
}
