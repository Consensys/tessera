package com.quorum.tessera.key.vault.hashicorp;

import static java.util.function.Predicate.not;

import com.quorum.tessera.key.vault.KeyVaultService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.core.VaultVersionedKeyValueOperations;
import org.springframework.vault.support.Versioned;

public class HashicorpKeyVaultService implements KeyVaultService {

  protected static final String SECRET_VERSION_KEY = "secretVersion";

  protected static final String SECRET_ID_KEY = "secretId";

  protected static final String SECRET_NAME_KEY = "secretName";

  protected static final String SECRET_ENGINE_NAME_KEY = "secretEngineName";

  private final VaultVersionedKeyValueTemplateFactory vaultVersionedKeyValueTemplateFactory;

  private final VaultOperations vaultOperations;

  HashicorpKeyVaultService(
      VaultOperations vaultOperations,
      Supplier<VaultVersionedKeyValueTemplateFactory>
          vaultVersionedKeyValueTemplateFactorySupplier) {
    this.vaultOperations = vaultOperations;
    this.vaultVersionedKeyValueTemplateFactory =
        vaultVersionedKeyValueTemplateFactorySupplier.get();
  }

  @Override
  public String getSecret(Map<String, String> hashicorpGetSecretData) {

    final String secretName = hashicorpGetSecretData.get(SECRET_NAME_KEY);
    final String secretEngineName = hashicorpGetSecretData.get(SECRET_ENGINE_NAME_KEY);
    final int secretVersion =
        Optional.ofNullable(hashicorpGetSecretData.get(SECRET_VERSION_KEY))
            .map(Integer::parseInt)
            .orElse(0);
    final String secretId = hashicorpGetSecretData.get(SECRET_ID_KEY);

    VaultVersionedKeyValueOperations keyValueOperations =
        vaultVersionedKeyValueTemplateFactory.createVaultVersionedKeyValueTemplate(
            vaultOperations, secretEngineName);

    Versioned<Map<String, Object>> versionedResponse =
        keyValueOperations.get(secretName, Versioned.Version.from(secretVersion));

    if (versionedResponse == null || !versionedResponse.hasData()) {
      throw new HashicorpVaultException("No data found at " + secretEngineName + "/" + secretName);
    }

    if (!versionedResponse.getData().containsKey(secretId)) {
      throw new HashicorpVaultException(
          "No value with id " + secretId + " found at " + secretEngineName + "/" + secretName);
    }

    return versionedResponse.getData().get(secretId).toString();
  }

  @Override
  public Object setSecret(Map<String, String> hashicorpSetSecretData) {

    String secretName = hashicorpSetSecretData.get(SECRET_NAME_KEY);
    String secretEngineName = hashicorpSetSecretData.get(SECRET_ENGINE_NAME_KEY);

    Map<String, String> nameValuePairs =
        hashicorpSetSecretData.entrySet().stream()
            .filter(
                not(
                    e ->
                        List.of(SECRET_NAME_KEY, SECRET_ID_KEY, SECRET_ENGINE_NAME_KEY)
                            .contains(e.getKey())))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    VaultVersionedKeyValueOperations keyValueOperations =
        vaultVersionedKeyValueTemplateFactory.createVaultVersionedKeyValueTemplate(
            vaultOperations, secretEngineName);
    try {
      return keyValueOperations.put(secretName, nameValuePairs);
    } catch (NullPointerException ex) {
      throw new HashicorpVaultException(
          "Unable to save generated secret to vault.  Ensure that the secret engine being used is a v2 kv secret engine");
    }
  }
}
