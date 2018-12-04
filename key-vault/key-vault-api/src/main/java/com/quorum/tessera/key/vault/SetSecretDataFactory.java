//package com.quorum.tessera.key.vault;
//
//import com.quorum.tessera.config.KeyVaultType;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.ServiceLoader;
//
//public interface SetSecretDataFactory {
//    SetSecretData create(Map<String, Object> data);
//
//    KeyVaultType getType();
//
//    static SetSecretDataFactory getInstance(KeyVaultType keyVaultType) {
//        List<SetSecretDataFactory> providers = new ArrayList<>();
//        ServiceLoader.load(SetSecretDataFactory.class).forEach(providers::add);
//
//        return providers.stream()
//                        .filter(factory -> factory.getType() == keyVaultType)
//                        .findFirst()
//                        .orElseThrow(() -> new IllegalArgumentException(keyVaultType + " implementation of SetSecretDataFactory was not found on the classpath"));
//    }
//}
