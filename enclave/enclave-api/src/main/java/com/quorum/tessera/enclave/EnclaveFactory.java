package com.quorum.tessera.enclave;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.encryption.KeyManagerImpl;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.nacl.NaclFacadeFactory;

import java.util.Collection;
import java.util.Optional;

/** Creates {@link Enclave} instances, which may point to remote services or local, in-app instances. */
public interface EnclaveFactory {

    default Enclave createLocal(Config config) {
        return createServer(config);
    }

    static Enclave createServer(Config config) {

        final KeyPairConverter keyPairConverter = new KeyPairConverter(config, new EnvironmentVariableProvider());
        final Collection<KeyPair> keys = keyPairConverter.convert(config.getKeys().getKeyData());

        final Collection<PublicKey> forwardKeys = keyPairConverter.convert(config.getAlwaysSendTo());

        return new EnclaveImpl(NaclFacadeFactory.newFactory().create(), new KeyManagerImpl(keys, forwardKeys));
    }

    /**
     * Determines from the provided configuration whether to construct a client to a remote service, or to create a
     * local instance.
     *
     * <p>If a remote instance is requested, it is constructed from a {@link EnclaveClientFactory}.
     *
     * @param config the global configuration to use to create a remote enclave connection
     * @return the {@link Enclave}, which may be either local or remote
     */
    default Enclave create(Config config) {
        final Optional<ServerConfig> enclaveServerConfig =
                config.getServerConfigs().stream().filter(sc -> sc.getApp() == AppType.ENCLAVE).findAny();

        if (enclaveServerConfig.isPresent()) {
            return EnclaveClientFactory.create().create(config);
        }

        return createServer(config);
    }

    static EnclaveFactory create() {
        return ServiceLoaderUtil.load(EnclaveFactory.class).orElse(new EnclaveFactory() {});
    }
}
