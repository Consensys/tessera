package com.quorum.tessera.key.vault.hashicorp;

import com.quorum.tessera.config.vault.data.HashicorpGetSecretData;
import com.quorum.tessera.config.vault.data.HashicorpSetSecretData;
import com.quorum.tessera.key.vault.KeyVaultService;
import java.util.Map;
import org.springframework.vault.support.Versioned;

public class HashicorpKeyVaultService
    implements KeyVaultService<HashicorpSetSecretData, HashicorpGetSecretData> {

  private final KeyValueOperationsDelegateFactory keyValueOperationsDelegateFactory;

  HashicorpKeyVaultService(KeyValueOperationsDelegateFactory keyValueOperationsDelegateFactory) {
    this.keyValueOperationsDelegateFactory = keyValueOperationsDelegateFactory;
  }

  @Override
  public String getSecret(HashicorpGetSecretData hashicorpGetSecretData) {
    KeyValueOperationsDelegate keyValueOperationsDelegate =
        keyValueOperationsDelegateFactory.create(hashicorpGetSecretData.getSecretEngineName());

    Versioned<Map<String, Object>> versionedResponse =
        keyValueOperationsDelegate.get(hashicorpGetSecretData);

    if (versionedResponse == null || !versionedResponse.hasData()) {
      throw new HashicorpVaultException(
          "No data found at "
              + hashicorpGetSecretData.getSecretEngineName()
              + "/"
              + hashicorpGetSecretData.getSecretName());
    }

    if (!versionedResponse.getData().containsKey(hashicorpGetSecretData.getValueId())) {
      throw new HashicorpVaultException(
          "No value with id "
              + hashicorpGetSecretData.getValueId()
              + " found at "
              + hashicorpGetSecretData.getSecretEngineName()
              + "/"
              + hashicorpGetSecretData.getSecretName());
    }

    return versionedResponse.getData().get(hashicorpGetSecretData.getValueId()).toString();
  }

  @Override
  public Object setSecret(HashicorpSetSecretData hashicorpSetSecretData) {
    KeyValueOperationsDelegate keyValueOperationsDelegate =
        keyValueOperationsDelegateFactory.create(hashicorpSetSecretData.getSecretEngineName());

    try {
      return keyValueOperationsDelegate.set(hashicorpSetSecretData);
    } catch (NullPointerException ex) {
      throw new HashicorpVaultException(
          "Unable to save generated secret to vault.  Ensure that the secret engine being used is a v2 kv secret engine");
    }
  }
}
