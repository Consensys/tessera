package com.quorum.tessera.context;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.apps.TesseraApp;
import com.quorum.tessera.config.apps.TesseraAppFactory;
import com.quorum.tessera.config.keypairs.*;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.config.util.KeyDataUtil;
import com.quorum.tessera.enclave.KeyPairConverter;
import com.quorum.tessera.encryption.KeyPair;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.server.TesseraServer;
import com.quorum.tessera.server.TesseraServerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface RuntimeContextFactory {

    default RuntimeContext create(Config config) {

        EncryptorConfig encryptorConfig = config.getEncryptor();
        KeyEncryptor keyEncryptor = KeyEncryptorFactory.newFactory().create(encryptorConfig);

        Validator validator = Validation.byDefaultProvider().configure()
            .ignoreXmlConfiguration()
            .buildValidatorFactory().getValidator();

        if(config.getKeys().getKeyVaultConfigs() != null
            && !config.getKeys().getKeyVaultConfigs().isEmpty()) {

            List<KeyVaultConfig> keyVaultConfigs = config.getKeys().getKeyVaultConfigs()
                .stream()
                .map(KeyVaultConfig.class::cast)
                .collect(Collectors.toList());

            Set<ConstraintViolation<?>> violations = keyVaultConfigs.stream()
                .map(validator::validate)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

            if(!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        }


        List<ConfigKeyPair> configKeyPairs = config.getKeys().getKeyData().stream()
            .map(o -> KeyDataUtil.unmarshal(o,keyEncryptor))
            .collect(Collectors.toList());

        Set<ConstraintViolation<?>> violations = configKeyPairs.stream()
            .map(validator::validate)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

        if(!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        EnvironmentVariableProvider environmentVariableProvider = new EnvironmentVariableProvider();

        KeyPairConverter keyPairConverter = new KeyPairConverter(config,environmentVariableProvider);

        List<KeyPair> pairs = new ArrayList<>(keyPairConverter.convert(configKeyPairs));

        List<TesseraServer> servers = config.getServerConfigs().stream()
            .map(serverConfig -> {
                TesseraApp app = TesseraAppFactory.create(serverConfig.getCommunicationType(), serverConfig.getApp())
                    .orElseThrow(
                        () ->
                            new IllegalStateException("Cant create app for " + serverConfig.getApp()));

                TesseraServerFactory tesseraServerFactory = TesseraServerFactory.create(serverConfig.getCommunicationType());

                return tesseraServerFactory.createServer(serverConfig, Collections.singleton(app));

            }).collect(Collectors.toList());


       List<PublicKey> alwaysSendTo = Stream.of(config)
            .map(Config::getAlwaysSendTo)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .map(Base64.getDecoder()::decode)
            .map(PublicKey::from)
            .collect(Collectors.toList());

        RuntimeContext runtimeContext = RuntimeContext.Builder.newBuilder()
            .withKeyEncryptor(keyEncryptor)
            .withServers(servers)
            .withPeers(config.getPeers().stream()
                .map(Peer::getUrl)
                .map(URI::create)
                .collect(Collectors.toList())
            )
            .withAlwaysSendTo(alwaysSendTo)
            .withKeys(pairs)
            .build();

        return runtimeContext;
    }

    static RuntimeContextFactory newFactory() {
        return new RuntimeContextFactory() {};
    }



}
