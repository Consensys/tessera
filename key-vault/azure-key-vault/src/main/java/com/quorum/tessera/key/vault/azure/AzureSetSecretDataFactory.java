//package com.quorum.tessera.key.vault.azure;
//
//import com.quorum.tessera.config.KeyVaultType;
//import com.quorum.tessera.key.vault.SetSecretData;
//import com.quorum.tessera.key.vault.SetSecretDataFactory;
//
//import java.util.Map;
//
//public class AzureSetSecretDataFactory implements SetSecretDataFactory {
//    @Override
//    public SetSecretData create(Map<String, Object> data) {
//        if(!data.containsKey("secretName") || !data.containsKey("secret")) {
//            throw new IllegalArgumentException("data must contain value with key 'secretName' and value with key 'secret'");
//        }
//
//        if(!(data.get("secretName") instanceof String) || !(data.get("secret") instanceof String)) {
//            throw new IllegalArgumentException("The values for keys 'secretPath' and 'secret' must be of type String for SetSecretData");
//        }
//
//        String secretName = (String) data.get("secretName");
//        String secret = (String) data.get("secret");
//
//        return new AzureSetSecretData(secretName, secret);
//    }
//
//    @Override
//    public KeyVaultType getType() {
//        return KeyVaultType.AZURE;
//    }
//}
