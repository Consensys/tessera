package com.quorum.tessera.context;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.KeyDataUtil;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.encryption.PublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.client.Client;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RuntimeContextProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeContextProvider.class);

    public static RuntimeContext provider() {
        LOGGER.debug("Providing RuntimeContext");
        RuntimeContextHolder contextHolder = RuntimeContextHolder.INSTANCE;
        if(contextHolder.getContext().isPresent()) {
            LOGGER.debug("Found stored RuntimeContext instance");
            return contextHolder.getContext().get();
        }

        Config config = ConfigFactory.create().getConfig();

        EncryptorConfig encryptorConfig =
            Optional.ofNullable(config.getEncryptor())
                .orElse(
                    new EncryptorConfig() {
                        {
                            setType(EncryptorType.NACL);
                        }
                    });

        KeyEncryptor keyEncryptor = KeyEncryptorFactory.newFactory().create(encryptorConfig);
        final KeyVaultConfigValidations vaultConfigValidation = KeyVaultConfigValidations.create();

        final RuntimeContextBuilder runtimeContextBuilder = RuntimeContextBuilder.create();

        if (Objects.nonNull(config.getKeys())) {

            List<ConfigKeyPair> configKeyPairs =
                config.getKeys().getKeyData().stream()
                    .map(o -> KeyDataUtil.unmarshal(o, keyEncryptor))
                    .collect(Collectors.toList());

            Set<ConstraintViolation<?>> violations = vaultConfigValidation.validate(config.getKeys(), configKeyPairs);

            if (!violations.isEmpty()) {
                LOGGER.debug("Constraint violations {}", violations);
                throw new ConstraintViolationException(violations);
            }

            final Enclave enclave = Enclave.create();
            runtimeContextBuilder.withKeys(enclave.getPublicKeys());
        }

        List<ServerConfig> servers = config.getServerConfigs();

        ServerConfig p2pServerContext =
            servers.stream()
                .filter(s -> s.getApp() == AppType.P2P)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No P2P server configured"));

        Client p2pClient = RestClientFactory.create().buildFrom(p2pServerContext);

        List<PublicKey> alwaysSendTo =
            Stream.of(config)
                .map(Config::getAlwaysSendTo)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(Base64.getDecoder()::decode)
                .map(PublicKey::from)
                .collect(Collectors.toList());

        RuntimeContext context =
            runtimeContextBuilder
                .withP2pServerUri(config.getP2PServerConfig().getServerUri())
                .withP2pClient(p2pClient)
                .withKeyEncryptor(keyEncryptor)
                .withDisablePeerDiscovery(config.isDisablePeerDiscovery())
                .withRemoteKeyValidation(config.getFeatures().isEnableRemoteKeyValidation())
                .withEnhancedPrivacy(config.getFeatures().isEnablePrivacyEnhancements())
                .withPeers(
                    config.getPeers().stream()
                        .map(Peer::getUrl)
                        .map(URI::create)
                        .collect(Collectors.toList()))
                .withAlwaysSendTo(alwaysSendTo)
                .withUseWhiteList(config.isUseWhiteList())
                .withRecoveryMode(config.isRecoveryMode())
                .build();

        contextHolder.setContext(context);
        return context;


    }

}
