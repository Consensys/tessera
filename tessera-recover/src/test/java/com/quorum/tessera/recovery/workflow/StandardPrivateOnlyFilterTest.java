package com.quorum.tessera.recovery.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
              EncodedPayload psvPayload = mock(EncodedPayload.class);
              when(psvPayload.getPrivacyMode()).thenReturn(p);

              if (p == PrivacyMode.MANDATORY_RECIPIENTS) {
                when(psvPayload.getMandatoryRecipients()).thenReturn(Set.of(mock(PublicKey.class)));
              }
              if (p == PrivacyMode.PRIVATE_STATE_VALIDATION) {
                when(psvPayload.getExecHash()).thenReturn("execHash".getBytes());
              }

              final BatchWorkflowContext contextPsv = new BatchWorkflowContext();
              contextPsv.setEncodedPayload(psvPayload);

              assertThat(filter.filter(contextPsv)).isFalse();
            });
  }

  @Test
  public void standardPrivatePasses() {
    final EncodedPayload payload = mock(EncodedPayload.class);
    when(payload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);

    final BatchWorkflowContext context = new BatchWorkflowContext();
    context.setEncodedPayload(payload);

    final boolean success = filter.filter(context);

    assertThat(success).isTrue();
  }
}
