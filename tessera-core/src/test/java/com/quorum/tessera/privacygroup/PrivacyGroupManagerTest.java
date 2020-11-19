package com.quorum.tessera.privacygroup;

import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.privacygroup.exception.PrivacyGroupNotFoundException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class PrivacyGroupManagerTest {

    private PrivacyGroupManager privacyGroupManager;

    @Before
    public void setUp() {
        privacyGroupManager = null;
    }

    @Test
    public void testCreatePrivacyGroup() {

        final PrivacyGroup privacyGroup = privacyGroupManager.createPrivacyGroup(
            "name", "description", List.of(mock(PublicKey.class)), new byte[1]
        );

        assertThat(privacyGroup).isNotNull();
        assertThat(privacyGroup.getType()).isEqualTo(PrivacyGroup.Type.PANTHEON);
    }

    @Test
    public void testCreateLegacyPrivacyGroup() {

        PublicKey member = mock(PublicKey.class);

        final PrivacyGroup privacyGroup = privacyGroupManager.createLegacyPrivacyGroup(List.of(member));

        assertThat(privacyGroup).isNotNull();
        assertThat(privacyGroup.getType()).isEqualTo(PrivacyGroup.Type.LEGACY);
    }

    @Test
    public void testFindPrivacyGroup() {
        PublicKey member = mock(PublicKey.class);

        final List<PrivacyGroup> privacyGroups = privacyGroupManager.findPrivacyGroup(List.of(member));

        assertThat(privacyGroups).isNotEmpty();
    }

    @Test
    public void testRetrievePrivacyGroup() {

        PublicKey groupId = mock(PublicKey.class);

        final PrivacyGroup result = privacyGroupManager.retrievePrivacyGroup(groupId);

        assertThat(result).isNotNull();
    }

    @Test
    public void testRetrievePrivacyGroupNotFound() {
        PublicKey groupId = mock(PublicKey.class);

        try {
            final PrivacyGroup result = privacyGroupManager.retrievePrivacyGroup(groupId);
        }
        catch(Exception ex) {
            assertThat(ex).isInstanceOf(PrivacyGroupNotFoundException.class);
        }
    }

    @Test
    public void testStorePrivacyGroup() {
        final byte[] encoded = new byte[0];
        privacyGroupManager.storePrivacyGroup(encoded);
    }

}
