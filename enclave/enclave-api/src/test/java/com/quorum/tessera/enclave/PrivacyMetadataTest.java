package com.quorum.tessera.enclave;

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

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
        assertThat(metaData.getPrivacyGroupId().get()).isEqualTo(PrivacyGroup.Id.fromBytes("GROUP_ID".getBytes()));
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
}
