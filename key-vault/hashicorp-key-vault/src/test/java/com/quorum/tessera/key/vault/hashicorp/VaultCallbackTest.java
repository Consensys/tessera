package com.quorum.tessera.key.vault.hashicorp;

import com.bettercloud.vault.VaultException;
import org.junit.Test;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class VaultCallbackTest {

    @Test(expected = HashicorpVaultException.class)
    public void executeThrowsSQLException() throws Exception {

        VaultCallback callback = mock(VaultCallback.class);

        doThrow(VaultException.class).when(callback).doExecute();

        VaultCallback.execute(callback);

    }

}
