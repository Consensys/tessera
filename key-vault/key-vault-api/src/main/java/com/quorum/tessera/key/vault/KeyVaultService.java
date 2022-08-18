package com.quorum.tessera.key.vault;

import java.util.Map;

public interface KeyVaultService {

  String getSecret(Map<String, String> getSecretData);

  SetSecretResponse setSecret(Map<String, String> setSecretData);
}
