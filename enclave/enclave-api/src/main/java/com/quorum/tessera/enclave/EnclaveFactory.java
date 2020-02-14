package com.quorum.tessera.enclave;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.config.util.KeyDataUtil;
import com.quorum.tessera.encryption.Encryptor;
import com.quorum.tessera.encryption.KeyManagerImpl;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.encryption.EncryptorFactory;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

/** Creates {@link Enclave} instances, which may point to remote services or local, in-app instances. */
public interface EnclaveFactory {

    default Enclave createLocal(Config config) {
        return createServer(config);
    }

    static Enclave createServer(Config config) {

        LoggerFactory.getLogger(EnclaveFactory.class).info("Creating enclave server");

        EncryptorConfig encryptorConfig = config.getEncryptor();
        EncryptorFactory encryptorFactory = EncryptorFactory.newFactory(encryptorConfig.getType().name());
        Encryptor encryptor = encryptorFactory.create(encryptorConfig.getProperties());
        KeyEncryptor keyEncryptor = KeyEncryptorFactory.newFactory().create(encryptorConfig);

        final KeyPairConverter keyPairConverter = new KeyPairConverter(config, new EnvironmentVariableProvider());
        final Collection<KeyPair> keys =
                keyPairConverter.convert(
                        config.getKeys().getKeyData().stream()
                                .map(kd -> KeyDataUtil.unmarshal(kd, keyEncryptor))
                                .collect(Collectors.toList()));

        final Collection<PublicKey> forwardKeys = keyPairConverter.convert(config.getAlwaysSendTo());

        LoggerFactory.getLogger(EnclaveFactory.class).info("Creating enclave");

        Enclave enclave = new EnclaveImpl(encryptor, new KeyManagerImpl(keys, forwardKeys));

        LoggerFactory.getLogger(EnclaveFactory.class).info("Created enclave {}", enclave);

        return enclave;
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
        LoggerFactory.getLogger(EnclaveFactory.class).info("Creating enclave");
        try {
            final Optional<ServerConfig> enclaveServerConfig =
                    config.getServerConfigs().stream().filter(sc -> sc.getApp() == AppType.ENCLAVE).findAny();

            if (enclaveServerConfig.isPresent()) {
                LoggerFactory.getLogger(EnclaveFactory.class).info("Creating remoted enclave");
                return EnclaveClientFactory.create().create(config);
            }
            return createServer(config);
        } catch (Throwable ex) {
            LoggerFactory.getLogger(EnclaveFactory.class).error("", ex);
            throw ex;
        }
    }

    static EnclaveFactory create() {
        return ServiceLoaderUtil.load(EnclaveFactory.class).orElse(new EnclaveFactory() {});
    }
}
