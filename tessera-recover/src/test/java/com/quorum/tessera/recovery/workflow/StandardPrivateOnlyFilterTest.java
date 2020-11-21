package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PrivacyMode;
import org.junit.Test;

import java.util.Arrays;

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
        Arrays.stream(PrivacyMode.values())
            .filter(p -> !PrivacyMode.STANDARD_PRIVATE.equals(p))
            .forEach(p -> {
                final EncodedPayload psvPayload = EncodedPayload.Builder.create().withPrivacyMode(p).build();
                final BatchWorkflowContext contextPsv = new BatchWorkflowContext();
                contextPsv.setEncodedPayload(psvPayload);

                assertThat(filter.filter(contextPsv)).isFalse();
            });
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
