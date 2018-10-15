package com.quorum.tessera.encryption;

import java.util.Collections;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;

public class EncodedPayloadWithRecipientsTest {

    @Test
    public void createWithEmpyKeyList() {
        final EncodedPayload encodedPayload = mock(EncodedPayload.class);

        final List<PublicKey> recipientKeys = Collections.EMPTY_LIST;

        EncodedPayloadWithRecipients encodedPayloadWithRecipients
                = new EncodedPayloadWithRecipients(encodedPayload, recipientKeys);

        assertThat(encodedPayloadWithRecipients.getEncodedPayload()).isSameAs(encodedPayload);
        assertThat(encodedPayloadWithRecipients.getRecipientKeys()).isEmpty();
    }

}
