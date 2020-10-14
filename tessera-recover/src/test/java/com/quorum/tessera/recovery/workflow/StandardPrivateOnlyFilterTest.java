package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PrivacyMode;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StandardPrivateOnlyFilterTest {

    private StandardPrivateOnlyFilter filter = new StandardPrivateOnlyFilter();

    @Test
    public void nullInputDoesntPass() {
        final BatchWorkflowContext context = new BatchWorkflowContext();

        final boolean success = filter.filter(context);

        assertThat(success).isFalse();
    }

    @Test
    public void nonStandardPrivateDoesntPass() {
        final EncodedPayload payload = EncodedPayload.Builder.create()
            .withPrivacyMode(PrivacyMode.PARTY_PROTECTION).build();
        final BatchWorkflowContext context = new BatchWorkflowContext();
        context.setEncodedPayload(payload);
        final boolean success = filter.filter(context);
        assertThat(success).isFalse();

        final EncodedPayload psvPayload = EncodedPayload.Builder.create()
            .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION).build();
        final BatchWorkflowContext contextPsv = new BatchWorkflowContext();
        contextPsv.setEncodedPayload(psvPayload);
        final boolean successPsv = filter.filter(contextPsv);
        assertThat(successPsv).isFalse();
    }

    @Test
    public void standardPrivatePasses() {
        final EncodedPayload payload = EncodedPayload.Builder.create()
            .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
            .build();
        final BatchWorkflowContext context = new BatchWorkflowContext();
        context.setEncodedPayload(payload);

        final boolean success = filter.filter(context);

        assertThat(success).isTrue();
    }
}
