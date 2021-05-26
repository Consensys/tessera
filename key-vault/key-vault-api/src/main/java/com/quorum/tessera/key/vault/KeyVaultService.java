package com.quorum.tessera.key.vault;

import java.util.Map;

public interface KeyVaultService {

  String getSecret(Map<String, String> getSecretData);

  Object setSecret(Map<String, String> setSecretData);
}
