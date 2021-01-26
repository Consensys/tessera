package net.consensys.tessera.migration.data;

import net.consensys.orion.enclave.EncryptedPayload;
import net.consensys.orion.enclave.PrivacyGroupPayload;
import net.consensys.tessera.migration.OrionKeyHelper;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class RecipientBoxHelperTest {

    @Test
    public void resolveRecipientBoxes() {


        OrionKeyHelper orionKeyHelper = mock(OrionKeyHelper.class);
        EncryptedPayload encryptedPayload = mock(EncryptedPayload.class);
        PrivacyGroupPayload privacyGroupPayload = mock(PrivacyGroupPayload.class);

        RecipientBoxHelper recipientBoxHelper = new RecipientBoxHelper(orionKeyHelper,encryptedPayload,privacyGroupPayload);

        recipientBoxHelper.getRecipientMapping();

        fail("TODO");

    }
}
