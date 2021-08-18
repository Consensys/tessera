package com.quorum.tessera.recovery.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import java.util.Arrays;
import java.util.Set;
import org.junit.Test;

public class StandardPrivateOnlyFilterTest {

  private final StandardPrivateOnlyFilter filter = new StandardPrivateOnlyFilter();

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
        .forEach(
            p -> {
              final EncodedPayload.Builder builder =
                  EncodedPayload.Builder.create().withPrivacyMode(p);
              if (p == PrivacyMode.MANDATORY_RECIPIENTS) {
                builder.withMandatoryRecipients(Set.of(mock(PublicKey.class)));
              }
              if (p == PrivacyMode.PRIVATE_STATE_VALIDATION) {
                builder.withExecHash("execHash".getBytes());
              }
              final EncodedPayload psvPayload = builder.build();
              final BatchWorkflowContext contextPsv = new BatchWorkflowContext();
              contextPsv.setEncodedPayload(psvPayload);

              assertThat(filter.filter(contextPsv)).isFalse();
            });
  }

  @Test
  public void standardPrivatePasses() {
    final EncodedPayload payload =
        EncodedPayload.Builder.create().withPrivacyMode(PrivacyMode.STANDARD_PRIVATE).build();
    final BatchWorkflowContext context = new BatchWorkflowContext();
    context.setEncodedPayload(payload);

    final boolean success = filter.filter(context);

    assertThat(success).isTrue();
  }
}
