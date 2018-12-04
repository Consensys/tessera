//package com.quorum.tessera.key.vault;
//
//import com.quorum.tessera.config.KeyVaultType;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.ServiceLoader;
//
//public interface GetSecretDataFactory {
//    GetSecretData create(Map<String, String> data);
//
//    KeyVaultType getType();
//
//    static GetSecretDataFactory getInstance(KeyVaultType keyVaultType) {
//        List<GetSecretDataFactory> providers = new ArrayList<>();
//        ServiceLoader.load(GetSecretDataFactory.class).forEach(providers::add);
//
//        return providers.stream()
//                        .filter(factory -> factory.getType() == keyVaultType)
//                        .findFirst()
//                        .orElseThrow(() -> new IllegalArgumentException(keyVaultType + " implementation of GetSecretDataFactory was not found on the classpath"));
//    }
//}
