package com.quorum.tessera.enclave;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.encryption.KeyManagerImpl;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.keypairconverter.KeyPairConverter;
import com.quorum.tessera.nacl.NaclFacadeFactory;
import java.util.Collection;
import java.util.Optional;

public interface EnclaveFactory {

    static Enclave createServer(Config config) {
        
        KeyPairConverter keyPairConverter = new KeyPairConverter(config, new EnvironmentVariableProvider());
        Collection<KeyPair> keys = keyPairConverter.convert(config.getKeys().getKeyData());

        Collection<PublicKey> forwardKeys = com.quorum.tessera.encryption.KeyFactory.convert(config.getAlwaysSendTo());
    
        return new EnclaveImpl(NaclFacadeFactory.newFactory().create(), new KeyManagerImpl(keys, forwardKeys));
    }
    
    default Enclave create(Config config) {
            Optional<ServerConfig> enclaveServerConfig = config.getServerConfigs().stream()
                .filter(sc -> sc.getApp() == AppType.ENCLAVE)
                .findAny();

        if (enclaveServerConfig.isPresent()) {
            return EnclaveClientFactory.create().create(config);
        }
        return createServer(config);

    }

    static EnclaveFactory create() {
        return ServiceLoaderUtil.load(EnclaveFactory.class)
                .orElse(new EnclaveFactory() {});
    }

}
