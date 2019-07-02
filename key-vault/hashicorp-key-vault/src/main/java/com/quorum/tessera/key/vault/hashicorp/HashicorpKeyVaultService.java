package com.quorum.tessera.key.vault.hashicorp;

import com.quorum.tessera.config.vault.data.GetSecretData;
import com.quorum.tessera.config.vault.data.HashicorpGetSecretData;
import com.quorum.tessera.config.vault.data.HashicorpSetSecretData;
import com.quorum.tessera.config.vault.data.SetSecretData;
import com.quorum.tessera.key.vault.KeyVaultException;
import com.quorum.tessera.key.vault.KeyVaultService;
import org.springframework.vault.support.Versioned;

import java.util.Map;

public class HashicorpKeyVaultService implements KeyVaultService {

    private final KeyValueOperationsDelegateFactory keyValueOperationsDelegateFactory;

    HashicorpKeyVaultService(KeyValueOperationsDelegateFactory keyValueOperationsDelegateFactory) {
        this.keyValueOperationsDelegateFactory = keyValueOperationsDelegateFactory;
    }

    @Override
    public String getSecret(GetSecretData getSecretData) {
        if (!(getSecretData instanceof HashicorpGetSecretData)) {
            throw new KeyVaultException(
                    "Incorrect data type passed to HashicorpKeyVaultService.  Type was " + getSecretData.getType());
        }

        HashicorpGetSecretData hashicorpGetSecretData = (HashicorpGetSecretData) getSecretData;

        KeyValueOperationsDelegate keyValueOperationsDelegate =
                keyValueOperationsDelegateFactory.create(hashicorpGetSecretData.getSecretEngineName());

        Versioned<Map<String, Object>> versionedResponse = keyValueOperationsDelegate.get(hashicorpGetSecretData);

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
    public Object setSecret(SetSecretData setSecretData) {
        if (!(setSecretData instanceof HashicorpSetSecretData)) {
            throw new KeyVaultException(
                    "Incorrect data type passed to HashicorpKeyVaultService.  Type was " + setSecretData.getType());
        }

        HashicorpSetSecretData hashicorpSetSecretData = (HashicorpSetSecretData) setSecretData;

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
