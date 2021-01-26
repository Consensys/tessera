package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.PublicKey;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class PrivacyGroupTest {

    @Test
    public void buildWithEverything() {

        PrivacyGroupId privacyGroupId = mock(PrivacyGroupId.class);
        List<PublicKey> recipients = List.of(mock(PublicKey.class));
        byte[] seed = "seed".getBytes();

        PrivacyGroup privacyGroup =
                PrivacyGroup.Builder.create()
                        .withPrivacyGroupId(privacyGroupId)
                        .withName("name")
                        .withDescription("description")
                        .withMembers(recipients)
                        .withSeed(seed)
                        .withState(PrivacyGroup.State.ACTIVE)
                        .withType(PrivacyGroup.Type.PANTHEON)
                        .build();

        assertThat(privacyGroup).isNotNull();
        assertThat(privacyGroup.getPrivacyGroupId()).isSameAs(privacyGroupId);
        assertThat(privacyGroup.getName()).isEqualTo("name");
        assertThat(privacyGroup.getDescription()).isEqualTo("description");
        assertThat(privacyGroup.getMembers()).containsAll(recipients);
        assertThat(privacyGroup.getSeed()).isEqualTo("seed".getBytes());
        assertThat(privacyGroup.getState()).isEqualTo(PrivacyGroup.State.ACTIVE);
        assertThat(privacyGroup.getType()).isEqualTo(PrivacyGroup.Type.PANTHEON);
    }

    @Test
    public void buildFrom() {
        final PrivacyGroupId privacyGroupId = mock(PrivacyGroupId.class);
        final List<PublicKey> recipients = List.of(mock(PublicKey.class));
        byte[] seed = "seed".getBytes();

        final PrivacyGroup privacyGroup =
                PrivacyGroup.Builder.create()
                        .withPrivacyGroupId(privacyGroupId)
                        .withName("name")
                        .withDescription("description")
                        .withMembers(recipients)
                        .withSeed(seed)
                        .withState(PrivacyGroup.State.ACTIVE)
                        .withType(PrivacyGroup.Type.PANTHEON)
                        .build();

        final PrivacyGroup anotherPrivacyGroup =
                PrivacyGroup.Builder.create().from(privacyGroup).withState(PrivacyGroup.State.DELETED).build();

        assertThat(anotherPrivacyGroup).isNotNull();
        assertThat(anotherPrivacyGroup.getPrivacyGroupId()).isSameAs(privacyGroupId);
        assertThat(anotherPrivacyGroup.getName()).isEqualTo("name");
        assertThat(anotherPrivacyGroup.getDescription()).isEqualTo("description");
        assertThat(anotherPrivacyGroup.getMembers()).containsAll(recipients);
        assertThat(anotherPrivacyGroup.getSeed()).isEqualTo("seed".getBytes());
        assertThat(anotherPrivacyGroup.getState()).isEqualTo(PrivacyGroup.State.DELETED);
        assertThat(anotherPrivacyGroup.getType()).isEqualTo(PrivacyGroup.Type.PANTHEON);
    }

    @Test(expected = NullPointerException.class)
    public void buildWithoutId() {
        PrivacyGroup.Builder.create().build();
    }

    @Test(expected = NullPointerException.class)
    public void buildWithoutMembers() {
        PrivacyGroup.Builder.create().withPrivacyGroupId(mock(PrivacyGroupId.class)).build();
    }

    @Test(expected = NullPointerException.class)
    public void buildWithoutState() {
        PrivacyGroup.Builder.create().withPrivacyGroupId(mock(PrivacyGroupId.class)).withMembers(List.of()).build();
    }

    @Test(expected = NullPointerException.class)
    public void buildWithoutType() {
        PrivacyGroup.Builder.create()
                .withPrivacyGroupId(mock(PrivacyGroupId.class))
                .withMembers(List.of())
                .withState(PrivacyGroup.State.ACTIVE)
                .build();
    }
}
