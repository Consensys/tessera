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
    public void noGroupConfig() {

        PublicKey localKey1 = mock(PublicKey.class);
        PublicKey localKey2 = mock(PublicKey.class);

        when(privacyGroupManager.getManagedKeys()).thenReturn(Set.of(localKey1, localKey2));

        residentGroupHandler.onCreate(mock(Config.class));

        verify(privacyGroupManager).findPrivacyGroupByType(eq(PrivacyGroup.Type.RESIDENT));
        verify(privacyGroupManager).getManagedKeys();

        verify(privacyGroupManager).saveResidentGroup(eq("private"), anyString(), memberCaptor.capture());

        assertThat(memberCaptor.getValue()).containsExactlyInAnyOrder(localKey1, localKey2);
    }

    @Test
    public void noGroupConfigAddKey() {

        PublicKey localKey1 = mock(PublicKey.class);
        PublicKey localKey2 = mock(PublicKey.class);
        PublicKey anotherKey = mock(PublicKey.class);

        final PrivacyGroup defaultGroup =
                PrivacyGroup.Builder.buildResidentGroup("private", "", List.of(localKey1, localKey2));

        when(privacyGroupManager.getManagedKeys()).thenReturn(Set.of(localKey1, localKey2, anotherKey));

        when(privacyGroupManager.findPrivacyGroupByType(eq(PrivacyGroup.Type.RESIDENT)))
                .thenReturn(List.of(defaultGroup));

        residentGroupHandler.onCreate(mock(Config.class));

        verify(privacyGroupManager).findPrivacyGroupByType(eq(PrivacyGroup.Type.RESIDENT));
        verify(privacyGroupManager).getManagedKeys();
        verify(privacyGroupManager).saveResidentGroup(eq("private"), anyString(), memberCaptor.capture());

        assertThat(memberCaptor.getValue()).containsExactlyInAnyOrder(localKey1, localKey2, anotherKey);
    }

    @Test
    public void noGroupConfigUpdateButOldKeyMaintained() {

        PublicKey localKey1 = mock(PublicKey.class);
        PublicKey localKey2 = mock(PublicKey.class);
        PublicKey anotherKey = mock(PublicKey.class);

        final PrivacyGroup defaultGroup =
                PrivacyGroup.Builder.buildResidentGroup("private", "", List.of(localKey1, localKey2));

        when(privacyGroupManager.getManagedKeys()).thenReturn(Set.of(anotherKey));

        when(privacyGroupManager.findPrivacyGroupByType(eq(PrivacyGroup.Type.RESIDENT)))
                .thenReturn(List.of(defaultGroup));

        residentGroupHandler.onCreate(mock(Config.class));

        verify(privacyGroupManager).findPrivacyGroupByType(eq(PrivacyGroup.Type.RESIDENT));
        verify(privacyGroupManager).getManagedKeys();
        verify(privacyGroupManager).saveResidentGroup(eq("private"), anyString(), memberCaptor.capture());

        assertThat(memberCaptor.getValue()).containsExactlyInAnyOrder(anotherKey, localKey1, localKey2);
    }

    @Test
    public void noGroupConfigButKeyBelongToAnotherGroup() {

        PublicKey localKey1 = mock(PublicKey.class);
        PublicKey localKey2 = mock(PublicKey.class);
        PublicKey anotherKey = mock(PublicKey.class);

        final PrivacyGroup oldGroup = mock(PrivacyGroup.class);
        when(oldGroup.getMembers()).thenReturn(List.of(localKey1, localKey2));
        when(oldGroup.getId()).thenReturn(PrivacyGroup.Id.fromBytes("oldGroup".getBytes()));

        when(privacyGroupManager.getManagedKeys()).thenReturn(Set.of(localKey2, anotherKey));

        when(privacyGroupManager.findPrivacyGroupByType(eq(PrivacyGroup.Type.RESIDENT))).thenReturn(List.of(oldGroup));

        assertThatThrownBy(() -> residentGroupHandler.onCreate(mock(Config.class)))
                .isInstanceOf(PrivacyViolationException.class)
                .hasMessageContaining("A local owned key cannot belong to more than one resident group");

        verify(privacyGroupManager).findPrivacyGroupByType(eq(PrivacyGroup.Type.RESIDENT));
        verify(privacyGroupManager).getManagedKeys();
    }

    @Test
    public void noGroupConfigExistingOtherResidentGroups() {

        PublicKey localKey1 = mock(PublicKey.class);
        PublicKey localKey2 = mock(PublicKey.class);
        PublicKey anotherKey = mock(PublicKey.class);

        final PrivacyGroup otherGroup = mock(PrivacyGroup.class);
        when(otherGroup.getMembers()).thenReturn(List.of(localKey1, localKey2));
        when(otherGroup.getId()).thenReturn(PrivacyGroup.Id.fromBytes("otherGroup".getBytes()));

        when(privacyGroupManager.getManagedKeys()).thenReturn(Set.of(anotherKey));

        when(privacyGroupManager.findPrivacyGroupByType(eq(PrivacyGroup.Type.RESIDENT)))
                .thenReturn(List.of(otherGroup));

        residentGroupHandler.onCreate(mock(Config.class));

        verify(privacyGroupManager).findPrivacyGroupByType(eq(PrivacyGroup.Type.RESIDENT));
        verify(privacyGroupManager).getManagedKeys();
        verify(privacyGroupManager).saveResidentGroup(eq("private"), anyString(), memberCaptor.capture());

        assertThat(memberCaptor.getValue()).containsExactly(anotherKey);
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
        verify(privacyGroupManager, times(4)).saveResidentGroup(anyString(), any(), any());
    }

    @Test
    public void keysThatAreLeftOutGetsAddedToDefaultGroup() {
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

        residentGroupHandler.onCreate(config);

        verify(privacyGroupManager).findPrivacyGroupByType(eq(PrivacyGroup.Type.RESIDENT));
        verify(privacyGroupManager).getManagedKeys();
        verify(privacyGroupManager).saveResidentGroup(eq("rg1"), any(), any());
        verify(privacyGroupManager).saveResidentGroup(eq("rg2"), any(), any());
        verify(privacyGroupManager).saveResidentGroup(eq("private"), anyString(), memberCaptor.capture());

        assertThat(memberCaptor.getValue()).containsExactly(PublicKey.from("m3".getBytes()));
    }

    @Test
    public void newLeftOutKeyCanStillBeAddedToExistedDefaultGroup() {
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

        residentGroupHandler.onCreate(config);

        verify(privacyGroupManager).findPrivacyGroupByType(eq(PrivacyGroup.Type.RESIDENT));
        verify(privacyGroupManager).getManagedKeys();
        verify(privacyGroupManager).saveResidentGroup(eq("rg1"), any(), any());
        verify(privacyGroupManager).saveResidentGroup(eq("private"), anyString(), memberCaptor.capture());

        assertThat(memberCaptor.getValue())
                .containsExactly(PublicKey.from("m2".getBytes()), PublicKey.from("m3".getBytes()));
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
                .hasMessageContaining("A local owned key cannot belong to more than one resident group");

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
                .hasMessageContaining("A local owned key cannot belong to more than one resident group");

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
        verify(privacyGroupManager).saveResidentGroup(eq("private"), any(), eq(List.of()));
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
        rg2.setMembers(List.of(PublicKey.from("m2".getBytes()).encodeToBase64()));
        rg2.setName("rg2");

        Config config = mock(Config.class);
        when(config.getResidentGroups()).thenReturn(List.of(rg2));

        when(privacyGroupManager.getManagedKeys())
                .thenReturn(Set.of(PublicKey.from("m1".getBytes()), PublicKey.from("m2".getBytes())));

        assertThatThrownBy(() -> residentGroupHandler.onCreate(config))
                .isInstanceOf(PrivacyViolationException.class)
                .hasMessageContaining("A local owned key cannot belong to more than one resident group");

        verify(privacyGroupManager).findPrivacyGroupByType(eq(PrivacyGroup.Type.RESIDENT));
        verify(privacyGroupManager).getManagedKeys();
    }

    @Test
    public void keysCanNotBePrivateDefaultAndThenGroup() {

        final PublicKey key = PublicKey.from("key".getBytes());
        final PrivacyGroup pg = PrivacyGroup.Builder.buildResidentGroup("private", "", List.of(key));

        when(privacyGroupManager.findPrivacyGroupByType(eq(PrivacyGroup.Type.RESIDENT))).thenReturn(List.of(pg));

        ResidentGroup rg1 = new ResidentGroup();
        rg1.setMembers(List.of(key.encodeToBase64()));
        rg1.setName("rg1");

        when(privacyGroupManager.getManagedKeys()).thenReturn(Set.of(key));

        Config config = mock(Config.class);
        when(config.getResidentGroups()).thenReturn(List.of(rg1));

        assertThatThrownBy(() -> residentGroupHandler.onCreate(config))
                .isInstanceOf(PrivacyViolationException.class)
                .hasMessageContaining("A local owned key cannot belong to more than one resident group");

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
                .hasMessageContaining("Keys configured in resident groups need to be locally managed");

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
