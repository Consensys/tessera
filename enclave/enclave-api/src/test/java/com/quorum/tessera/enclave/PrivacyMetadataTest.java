package com.quorum.tessera.enclave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.quorum.tessera.encryption.PublicKey;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class PrivacyMetadataTest {

  @Test
  public void forStandardPrivate() {
    final PrivacyMetadata metaData = PrivacyMetadata.Builder.forStandardPrivate().build();

    assertThat(metaData).isNotNull();
    assertThat(metaData.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
    assertThat(metaData.getPrivacyGroupId()).isNotPresent();
  }

  @Test
  public void forStandardPrivateWithGroupId() {
    final PrivacyMetadata metaData =
        PrivacyMetadata.Builder.create()
            .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
            .withPrivacyGroupId(PrivacyGroup.Id.fromBytes("GROUP_ID".getBytes()))
            .build();

    assertThat(metaData).isNotNull();
    assertThat(metaData.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
    assertThat(metaData.getPrivacyGroupId()).isPresent();
    assertThat(metaData.getPrivacyGroupId().get())
        .isEqualTo(PrivacyGroup.Id.fromBytes("GROUP_ID".getBytes()));
  }

  @Test(expected = RuntimeException.class)
  public void forPartyProtectionInvalid() {
    PrivacyMetadata.Builder.create()
        .withPrivacyMode(PrivacyMode.PARTY_PROTECTION)
        .withExecHash("hash".getBytes())
        .build();
  }

  @Test(expected = RuntimeException.class)
  public void forPsvInvalid() {
    PrivacyMetadata.Builder.create()
        .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
        .withPrivacyGroupId(PrivacyGroup.Id.fromBytes("GROUP_ID".getBytes()))
        .withExecHash(null)
        .build();
  }

  @Test
  public void withEverything() {

    final AffectedTransaction affected = mock(AffectedTransaction.class);

    PrivacyGroup.Id id = PrivacyGroup.Id.fromBytes("GROUP_ID".getBytes());

    final PrivacyMetadata metaData =
        PrivacyMetadata.Builder.create()
            .withPrivacyMode(PrivacyMode.PRIVATE_STATE_VALIDATION)
            .withAffectedTransactions(List.of(affected))
            .withExecHash("execHash".getBytes())
            .withPrivacyGroupId(id)
            .build();

    assertThat(metaData).isNotNull();
    assertThat(metaData.getPrivacyMode()).isEqualTo(PrivacyMode.PRIVATE_STATE_VALIDATION);
    assertThat(metaData.getAffectedContractTransactions()).containsExactly(affected);
    assertThat(metaData.getExecHash()).isEqualTo("execHash".getBytes());
    assertThat(metaData.getPrivacyGroupId()).isPresent();
    assertThat(metaData.getPrivacyGroupId().get()).isEqualTo(id);
  }

  @Test
  public void buildMandatoryRecipients() {
    final AffectedTransaction affected = mock(AffectedTransaction.class);

    PrivacyGroup.Id id = PrivacyGroup.Id.fromBytes("GROUP_ID".getBytes());

    final PrivacyMetadata metaData =
        PrivacyMetadata.Builder.create()
            .withPrivacyMode(PrivacyMode.MANDATORY_RECIPIENTS)
            .withAffectedTransactions(List.of(affected))
            .withPrivacyGroupId(id)
            .withMandatoryRecipients(Set.of(PublicKey.from("KEY1".getBytes())))
            .build();

    assertThat(metaData).isNotNull();
    assertThat(metaData.getPrivacyMode()).isEqualTo(PrivacyMode.MANDATORY_RECIPIENTS);
    assertThat(metaData.getAffectedContractTransactions()).containsExactly(affected);
    assertThat(metaData.getPrivacyGroupId()).isPresent();
    assertThat(metaData.getPrivacyGroupId().get()).isEqualTo(id);
    assertThat(metaData.getMandatoryRecipients())
        .containsExactly(PublicKey.from("KEY1".getBytes()));
  }

  @Test(expected = RuntimeException.class)
  public void mandatoryRecipientsInvalid() {
    PrivacyMetadata.Builder.create().withPrivacyMode(PrivacyMode.MANDATORY_RECIPIENTS).build();
  }
}
