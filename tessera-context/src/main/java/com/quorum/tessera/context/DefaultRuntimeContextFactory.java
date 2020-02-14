package com.quorum.tessera.context;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.config.util.JaxbUtil;
import com.quorum.tessera.config.util.KeyDataUtil;
import com.quorum.tessera.enclave.KeyPairConverter;
import com.quorum.tessera.encryption.KeyPair;
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

class DefaultRuntimeContextFactory implements RuntimeContextFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRuntimeContextFactory.class);

    @Override
    public RuntimeContext create(Config config) {

        LOGGER.debug("Creating RuntimeContext from {}", JaxbUtil.marshalToStringNoValidation(config));
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

        List<ConfigKeyPair> configKeyPairs =
                config.getKeys().getKeyData().stream()
                        .map(o -> KeyDataUtil.unmarshal(o, keyEncryptor))
                        .collect(Collectors.toList());

        Set<ConstraintViolation<?>> violations = vaultConfigValidation.validate(config.getKeys(), configKeyPairs);

        if (!violations.isEmpty()) {
            LOGGER.debug("Constraint violations {}", violations);
            throw new ConstraintViolationException(violations);
        }

        KeyPairConverter keyPairConverter = new KeyPairConverter(config, new EnvironmentVariableProvider());
        LOGGER.debug("Converting key pairs {}", configKeyPairs);
        List<KeyPair> pairs = new ArrayList<>(keyPairConverter.convert(configKeyPairs));
        LOGGER.debug("Converted key pairs {}", configKeyPairs);

        LOGGER.debug("Creating servers from {}", config.getServerConfigs());
        List<ServerConfig> servers =
                config.getServerConfigs().stream()
                        .peek(s -> LOGGER.debug("Process server config {}", s))
                        // .map(ServerContext::from)
                        .collect(Collectors.toList());

        LOGGER.debug("Created {} servers", servers.size());

        ServerConfig p2pServerContext =
                servers.stream()
                        .filter(s -> s.getApp() == AppType.P2P)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No P2P server configured"));

        LOGGER.debug("Creating p2p Client with {}", p2pServerContext);
        Client p2pClient = RestClientFactory.create().buildFrom(p2pServerContext);
        LOGGER.debug("Created p2p client with {}", p2pServerContext);

        LOGGER.debug("Extraction always send to keys");
        List<PublicKey> alwaysSendTo =
                Stream.of(config)
                        .map(Config::getAlwaysSendTo)
                        .filter(Objects::nonNull)
                        .flatMap(List::stream)
                        .map(Base64.getDecoder()::decode)
                        .map(PublicKey::from)
                        .collect(Collectors.toList());

        LOGGER.debug("Extracted {} always send to keys", alwaysSendTo.size());

        LOGGER.debug("Building RuntimeContext from {}", JaxbUtil.marshalToStringNoValidation(config));

        RuntimeContext context =
                RuntimeContextBuilder.create()
                        .withP2pServerUri(config.getP2PServerConfig().getServerUri())
                        .withP2pClient(p2pClient)
                        .withKeyEncryptor(keyEncryptor)
                        .withDisablePeerDiscovery(config.isDisablePeerDiscovery())
                        .withRemoteKeyValidation(config.getFeatures().isEnableRemoteKeyValidation())
                        .withPeers(
                                config.getPeers().stream()
                                        .map(Peer::getUrl)
                                        .map(URI::create)
                                        .collect(Collectors.toList()))
                        .withAlwaysSendTo(alwaysSendTo)
                        .withKeys(pairs)
                        .withUseWhiteList(config.isUseWhiteList())
                        .build();

        LOGGER.debug("Created RuntimeContext from {}", JaxbUtil.marshalToStringNoValidation(config));

        return context;
    }
}
