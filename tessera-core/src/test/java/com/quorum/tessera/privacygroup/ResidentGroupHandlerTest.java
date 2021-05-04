package com.quorum.tessera.privacygroup;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.JdbcConfig;
import com.quorum.tessera.config.ResidentGroup;
import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.exception.PrivacyViolationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ResidentGroupHandlerTest {

    @Mock private PrivacyGroupManager privacyGroupManager;

    @Captor private ArgumentCaptor<List<PublicKey>> memberCaptor;

    @InjectMocks private ResidentGroupHandlerImpl residentGroupHandler;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(privacyGroupManager);
    }

    @Test
    public void noResidentGroupThrowsError() {

        PublicKey localKey1 = mock(PublicKey.class);
        PublicKey localKey2 = mock(PublicKey.class);

        when(privacyGroupManager.getManagedKeys()).thenReturn(Set.of(localKey1, localKey2));

        assertThatThrownBy(() -> residentGroupHandler.onCreate(mock(Config.class)))
                .isInstanceOf(PrivacyViolationException.class)
                .hasMessageContaining("must belong to a resident group");

        verify(privacyGroupManager).getManagedKeys();
        verify(privacyGroupManager).findPrivacyGroupByType(PrivacyGroup.Type.RESIDENT);
    }

    @Test
    public void noGroupConfigButExistedInDb() {

        PublicKey localKey1 = mock(PublicKey.class);
        PublicKey localKey2 = mock(PublicKey.class);
        when(privacyGroupManager.getManagedKeys()).thenReturn(Set.of(localKey1, localKey2));

        PrivacyGroup rg = mock(PrivacyGroup.class);
        when(rg.getMembers()).thenReturn(List.of(localKey1, localKey2));
        when(rg.getId()).thenReturn(PrivacyGroup.Id.fromBytes("id".getBytes()));

        when(privacyGroupManager.findPrivacyGroupByType(eq(PrivacyGroup.Type.RESIDENT))).thenReturn(List.of(rg));

        residentGroupHandler.onCreate(mock(Config.class));

        verify(privacyGroupManager).getManagedKeys();
        verify(privacyGroupManager).findPrivacyGroupByType(PrivacyGroup.Type.RESIDENT);
    }

    @Test
    public void createNewGroups() {
        ResidentGroup rg1 = new ResidentGroup();
        rg1.setMembers(List.of(PublicKey.from("m1".getBytes()).encodeToBase64()));
        rg1.setName("rg1");
        ResidentGroup rg2 = new ResidentGroup();
        rg2.setMembers(List.of(PublicKey.from("m2".getBytes()).encodeToBase64()));
        rg2.setName("rg2");
        ResidentGroup rg3 = new ResidentGroup();
        rg3.setMembers(List.of(PublicKey.from("m3".getBytes()).encodeToBase64()));
        rg3.setName("rg3");

        when(privacyGroupManager.getManagedKeys())
                .thenReturn(
                        Set.of(
                                PublicKey.from("m1".getBytes()),
                                PublicKey.from("m2".getBytes()),
                                PublicKey.from("m3".getBytes())));

        Config config = mock(Config.class);
        when(config.getResidentGroups()).thenReturn(List.of(rg1, rg2, rg3));

        residentGroupHandler.onCreate(config);

        verify(privacyGroupManager).findPrivacyGroupByType(eq(PrivacyGroup.Type.RESIDENT));
        verify(privacyGroupManager).getManagedKeys();
        verify(privacyGroupManager, times(3)).saveResidentGroup(anyString(), any(), any());
    }

    @Test
    public void homelessKeysNotAllowed() {
        ResidentGroup rg1 = new ResidentGroup();
        rg1.setMembers(List.of(PublicKey.from("m1".getBytes()).encodeToBase64()));
        rg1.setName("rg1");
        ResidentGroup rg2 = new ResidentGroup();
        rg2.setMembers(List.of(PublicKey.from("m2".getBytes()).encodeToBase64()));
        rg2.setName("rg2");

        when(privacyGroupManager.getManagedKeys())
                .thenReturn(
                        Set.of(
                                PublicKey.from("m1".getBytes()),
                                PublicKey.from("m2".getBytes()),
                                PublicKey.from("m3".getBytes())));

        Config config = mock(Config.class);
        when(config.getResidentGroups()).thenReturn(List.of(rg1, rg2));

        assertThatThrownBy(() -> residentGroupHandler.onCreate(config))
                .isInstanceOf(PrivacyViolationException.class)
                .hasMessageContaining("PublicKey[bTM=] must belong to a resident group");

        verify(privacyGroupManager).findPrivacyGroupByType(eq(PrivacyGroup.Type.RESIDENT));
        verify(privacyGroupManager).getManagedKeys();
    }

    @Test
    public void homelessKeysNotAllowedConsideringBothConfigAndDb() {
        ResidentGroup rg1 = new ResidentGroup();
        rg1.setMembers(List.of(PublicKey.from("m1".getBytes()).encodeToBase64()));
        rg1.setName("rg1");

        when(privacyGroupManager.getManagedKeys())
                .thenReturn(
                        Set.of(
                                PublicKey.from("m1".getBytes()),
                                PublicKey.from("m2".getBytes()),
                                PublicKey.from("m3".getBytes())));

        Config config = mock(Config.class);
        when(config.getResidentGroups()).thenReturn(List.of(rg1));

        final PrivacyGroup pg =
                PrivacyGroup.Builder.buildResidentGroup("private", "", List.of(PublicKey.from("m2".getBytes())));
        when(privacyGroupManager.findPrivacyGroupByType(eq(PrivacyGroup.Type.RESIDENT))).thenReturn(List.of(pg));

        assertThatThrownBy(() -> residentGroupHandler.onCreate(config))
                .isInstanceOf(PrivacyViolationException.class)
                .hasMessageContaining("PublicKey[bTM=] must belong to a resident group");

        verify(privacyGroupManager).findPrivacyGroupByType(eq(PrivacyGroup.Type.RESIDENT));
        verify(privacyGroupManager).getManagedKeys();
    }

    @Test
    public void keysCanNotExistInTwoDifferentGroups() {

        ResidentGroup rg1 = new ResidentGroup();
        rg1.setMembers(List.of(PublicKey.from("m1".getBytes()).encodeToBase64()));
        rg1.setName("rg1");
        ResidentGroup rg2 = new ResidentGroup();
        rg2.setMembers(List.of(PublicKey.from("m1".getBytes()).encodeToBase64()));
        rg2.setName("rg2");

        when(privacyGroupManager.getManagedKeys())
                .thenReturn(Set.of(PublicKey.from("m1".getBytes()), PublicKey.from("m2".getBytes())));

        Config config = mock(Config.class);
        when(config.getResidentGroups()).thenReturn(List.of(rg1, rg2));

        assertThatThrownBy(() -> residentGroupHandler.onCreate(config))
                .isInstanceOf(PrivacyViolationException.class)
                .hasMessageContaining("Key cannot belong to more than one resident group")
                .hasMessageContaining("PublicKey[bTE=]");

        verify(privacyGroupManager).findPrivacyGroupByType(eq(PrivacyGroup.Type.RESIDENT));
        verify(privacyGroupManager).getManagedKeys();
    }

    @Test
    public void keyExistedInADifferentGroupInDb() {

        ResidentGroup rg1 = new ResidentGroup();
        rg1.setMembers(List.of(PublicKey.from("m1".getBytes()).encodeToBase64()));
        rg1.setName("rg1");
        ResidentGroup rg2 = new ResidentGroup();
        rg2.setMembers(List.of(PublicKey.from("m2".getBytes()).encodeToBase64()));
        rg2.setName("rg2");

        when(privacyGroupManager.getManagedKeys())
                .thenReturn(Set.of(PublicKey.from("m1".getBytes()), PublicKey.from("m2".getBytes())));

        Config config = mock(Config.class);
        when(config.getResidentGroups()).thenReturn(List.of(rg1, rg2));

        final PrivacyGroup existedGroup = mock(PrivacyGroup.class);
        when(existedGroup.getMembers()).thenReturn(List.of(PublicKey.from("m2".getBytes())));
        when(existedGroup.getId()).thenReturn(PrivacyGroup.Id.fromBytes("otherGroup".getBytes()));

        when(privacyGroupManager.findPrivacyGroupByType(eq(PrivacyGroup.Type.RESIDENT)))
                .thenReturn(List.of(existedGroup));

        assertThatThrownBy(() -> residentGroupHandler.onCreate(config))
                .isInstanceOf(PrivacyViolationException.class)
                .hasMessageContaining("Key cannot belong to more than one resident group");

        verify(privacyGroupManager).findPrivacyGroupByType(eq(PrivacyGroup.Type.RESIDENT));
        verify(privacyGroupManager).getManagedKeys();
    }

    @Test
    public void keyGetsAddedToAnExistingGroup() {

        ResidentGroup rg1 = new ResidentGroup();
        rg1.setMembers(
                List.of(
                        PublicKey.from("m1".getBytes()).encodeToBase64(),
                        PublicKey.from("m2".getBytes()).encodeToBase64()));
        rg1.setName("rg1");

        Config config = mock(Config.class);
        when(config.getResidentGroups()).thenReturn(List.of(rg1));

        when(privacyGroupManager.getManagedKeys())
                .thenReturn(Set.of(PublicKey.from("m1".getBytes()), PublicKey.from("m2".getBytes())));

        final PrivacyGroup existedGroup = mock(PrivacyGroup.class);
        when(existedGroup.getMembers()).thenReturn(List.of(PublicKey.from("m1".getBytes())));
        when(existedGroup.getId()).thenReturn(PrivacyGroup.Id.fromBytes("rg1".getBytes()));

        when(privacyGroupManager.findPrivacyGroupByType(eq(PrivacyGroup.Type.RESIDENT)))
                .thenReturn(List.of(existedGroup));

        residentGroupHandler.onCreate(config);

        verify(privacyGroupManager).findPrivacyGroupByType(eq(PrivacyGroup.Type.RESIDENT));
        verify(privacyGroupManager).getManagedKeys();
        verify(privacyGroupManager).saveResidentGroup(eq("rg1"), any(), memberCaptor.capture());

        assertThat(memberCaptor.getValue())
                .containsExactlyInAnyOrder(PublicKey.from("m1".getBytes()), PublicKey.from("m2".getBytes()));
    }

    @Test
    public void keysCanNotBeMovedOutOfAGroup() {

        final PrivacyGroup existedGroup = mock(PrivacyGroup.class);
        when(existedGroup.getMembers()).thenReturn(List.of(PublicKey.from("m1".getBytes())));
        when(existedGroup.getId()).thenReturn(PrivacyGroup.Id.fromBytes("rg1".getBytes()));

        when(privacyGroupManager.findPrivacyGroupByType(eq(PrivacyGroup.Type.RESIDENT)))
                .thenReturn(List.of(existedGroup));

        ResidentGroup rg2 = new ResidentGroup();
        rg2.setMembers(
                List.of(
                        PublicKey.from("m1".getBytes()).encodeToBase64(),
                        PublicKey.from("m2".getBytes()).encodeToBase64()));
        rg2.setName("rg2");

        Config config = mock(Config.class);
        when(config.getResidentGroups()).thenReturn(List.of(rg2));

        when(privacyGroupManager.getManagedKeys())
                .thenReturn(Set.of(PublicKey.from("m1".getBytes()), PublicKey.from("m2".getBytes())));

        assertThatThrownBy(() -> residentGroupHandler.onCreate(config))
                .isInstanceOf(PrivacyViolationException.class)
                .hasMessageContaining("Key cannot belong to more than one resident group");

        verify(privacyGroupManager).findPrivacyGroupByType(eq(PrivacyGroup.Type.RESIDENT));
        verify(privacyGroupManager).getManagedKeys();
    }

    @Test
    public void residentGroupCanNotHaveRemoteKey() {

        ResidentGroup rg1 = new ResidentGroup();
        rg1.setMembers(List.of(PublicKey.from("m1".getBytes()).encodeToBase64()));
        rg1.setName("rg1");
        ResidentGroup rg2 = new ResidentGroup();
        rg2.setMembers(List.of(PublicKey.from("m2".getBytes()).encodeToBase64()));
        rg2.setName("rg2");

        Config config = mock(Config.class);
        when(config.getResidentGroups()).thenReturn(List.of(rg1));

        when(privacyGroupManager.getManagedKeys()).thenReturn(Set.of(PublicKey.from("m2".getBytes())));

        assertThatThrownBy(() -> residentGroupHandler.onCreate(config))
                .isInstanceOf(PrivacyViolationException.class)
                .hasMessageContaining("Key PublicKey[bTE=] configured in resident groups need to be locally managed");

        verify(privacyGroupManager).getManagedKeys();
    }

    @Test
    public void create() {

        JdbcConfig jdbcConfig = new JdbcConfig("username", "password", "jdbc:h2:mem:test");
        final Config config = mock(Config.class);
        when(config.getJdbcConfig()).thenReturn(jdbcConfig);

        ResidentGroupHandler instance = ResidentGroupHandler.create(config);
        assertThat(instance).isNotNull();
    }
}
