package com.quorum.tessera.privacygroup;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.JdbcConfig;
import com.quorum.tessera.data.PrivacyGroupDAO;
import com.quorum.tessera.data.PrivacyGroupEntity;
import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.enclave.PrivacyGroupUtil;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.privacygroup.exception.PrivacyGroupNotFoundException;
import com.quorum.tessera.privacygroup.publish.PrivacyGroupPublisher;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PrivacyGroupManagerTest {

    private PrivacyGroupManager privacyGroupManager;

    private PrivacyGroupDAO privacyGroupDAO;

    private PrivacyGroupUtil privacyGroupUtil;

    private PrivacyGroupPublisher publisher;

    @Before
    public void setUp() {
        privacyGroupDAO = mock(PrivacyGroupDAO.class);
        publisher = mock(PrivacyGroupPublisher.class);
        privacyGroupUtil = mock(PrivacyGroupUtil.class);
        privacyGroupManager = new PrivacyGroupManagerImpl(privacyGroupDAO, publisher, privacyGroupUtil);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(privacyGroupDAO, publisher);
    }

    @Test
    public void testCreatePrivacyGroup() {

        when(privacyGroupUtil.generatePrivacyGroupId(anyList(), any(byte[].class)))
            .thenReturn("generatedId".getBytes());
        when(privacyGroupUtil.generateLookupId(anyList())).thenReturn("lookup".getBytes());
        when(privacyGroupUtil.encode(any())).thenReturn("encoded".getBytes());

        final List<PublicKey> members = List.of(mock(PublicKey.class), mock(PublicKey.class));

        doAnswer(
            invocation -> {
                Callable callable = invocation.getArgument(1);
                callable.call();
                return mock(PrivacyGroupEntity.class);
            }).when(privacyGroupDAO).save(any(), any());

        final PrivacyGroup privacyGroup =
            privacyGroupManager.createPrivacyGroup("name", "description", members, new byte[1]);

        // Verify entity being saved has the correct values
        ArgumentCaptor<PrivacyGroupEntity> argCaptor = ArgumentCaptor.forClass(PrivacyGroupEntity.class);
        verify(privacyGroupDAO).save(argCaptor.capture(), any());
        PrivacyGroupEntity savedEntity = argCaptor.getValue();
        assertThat(savedEntity).isNotNull();
        assertThat(savedEntity.getId()).isEqualTo("generatedId".getBytes());
        assertThat(savedEntity.getLookupId()).isEqualTo("lookup".getBytes());
        assertThat(savedEntity.getData()).isEqualTo("encoded".getBytes());

        // Verify payload being distributed has the correct values
        ArgumentCaptor<byte[]> payloadCaptor = ArgumentCaptor.forClass(byte[].class);
        ArgumentCaptor<List<PublicKey>> recipientsCaptor = ArgumentCaptor.forClass(List.class);
        verify(publisher).publishPrivacyGroup(payloadCaptor.capture(), recipientsCaptor.capture());
        assertThat(payloadCaptor.getValue()).isEqualTo("encoded".getBytes());
        assertThat(recipientsCaptor.getValue()).containsAll(members);

        // Verify generated privacy group has the correct values
        assertThat(privacyGroup).isNotNull();
        assertThat(privacyGroup.getPrivacyGroupId().getKeyBytes()).isEqualTo("generatedId".getBytes());
        assertThat(privacyGroup.getName()).isEqualTo("name");
        assertThat(privacyGroup.getDescription()).isEqualTo("description");
        assertThat(privacyGroup.getMembers()).containsAll(members);
        assertThat(privacyGroup.getType()).isEqualTo(PrivacyGroup.Type.PANTHEON);
        assertThat(privacyGroup.getState()).isEqualTo(PrivacyGroup.State.ACTIVE);
    }

    @Test
    public void testCreateLegacyPrivacyGroup() {

        final List<PublicKey> members = List.of(mock(PublicKey.class), mock(PublicKey.class));
        when(privacyGroupUtil.generatePrivacyGroupId(anyList(), any()))
            .thenReturn("generatedId".getBytes());
        when(privacyGroupUtil.generateLookupId(anyList())).thenReturn("lookup".getBytes());
        when(privacyGroupUtil.encode(any())).thenReturn("encoded".getBytes());

        final PrivacyGroup privacyGroup = privacyGroupManager.createLegacyPrivacyGroup(members);

        // Verify entity being saved has the correct values
        ArgumentCaptor<PrivacyGroupEntity> argCaptor = ArgumentCaptor.forClass(PrivacyGroupEntity.class);
        verify(privacyGroupDAO).save(argCaptor.capture());
        PrivacyGroupEntity savedEntity = argCaptor.getValue();
        assertThat(savedEntity).isNotNull();
        assertThat(savedEntity.getId()).isEqualTo("generatedId".getBytes());
        assertThat(savedEntity.getLookupId()).isEqualTo("lookup".getBytes());
        assertThat(savedEntity.getData()).isEqualTo("encoded".getBytes());

        // Verify generated privacy group has the correct values
        assertThat(privacyGroup).isNotNull();
        assertThat(privacyGroup.getPrivacyGroupId().getKeyBytes()).isEqualTo("generatedId".getBytes());
        assertThat(privacyGroup.getName()).isEqualTo("legacy");
        assertThat(privacyGroup.getDescription()).isEqualTo("Privacy groups to support the creation of groups by privateFor and privateFrom");
        assertThat(privacyGroup.getMembers()).containsAll(members);
        assertThat(privacyGroup.getType()).isEqualTo(PrivacyGroup.Type.LEGACY);
        assertThat(privacyGroup.getState()).isEqualTo(PrivacyGroup.State.ACTIVE);
    }

    @Test
    public void testFindPrivacyGroup() {

        final PrivacyGroupEntity et1 = mock(PrivacyGroupEntity.class);
        when(et1.getData()).thenReturn("data1".getBytes());
        final PrivacyGroupEntity et2 = mock(PrivacyGroupEntity.class);
        when(et2.getData()).thenReturn("data2".getBytes());
        final List<PrivacyGroupEntity> dbResult = List.of(et1, et2);

        final PrivacyGroup pg1 = mock(PrivacyGroup.class);
        final PrivacyGroup pg2 = mock(PrivacyGroup.class);

        when(privacyGroupDAO.findByLookupId("lookup".getBytes())).thenReturn(dbResult);
        when(privacyGroupUtil.generateLookupId(anyList())).thenReturn("lookup".getBytes());
        when(privacyGroupUtil.decode("data1".getBytes())).thenReturn(pg1);
        when(privacyGroupUtil.decode("data2".getBytes())).thenReturn(pg2);


        final List<PrivacyGroup> privacyGroups = privacyGroupManager.findPrivacyGroup(List.of());

        assertThat(privacyGroups).isNotEmpty();
        assertThat(privacyGroups).contains(pg1, pg2);

        verify(privacyGroupDAO).findByLookupId("lookup".getBytes());
    }

    @Test
    public void testRetrievePrivacyGroup() {

        final PublicKey id = PublicKey.from("id".getBytes());
        final PrivacyGroupEntity mockResult = mock(PrivacyGroupEntity.class);
        final PrivacyGroup mockPrivacyGroup = mock(PrivacyGroup.class);
        when(mockResult.getData()).thenReturn("data".getBytes());

        when(privacyGroupUtil.decode("data".getBytes())).thenReturn(mockPrivacyGroup);

        when(privacyGroupDAO.retrieve("id".getBytes())).thenReturn(Optional.of(mockResult));

        final PrivacyGroup result = privacyGroupManager.retrievePrivacyGroup(id);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(mockPrivacyGroup);

        verify(privacyGroupDAO).retrieve("id".getBytes());
    }

    @Test
    public void testRetrievePrivacyGroupNotFound() {

        final PublicKey groupId = PublicKey.from("id".getBytes());
        when(privacyGroupDAO.retrieve("id".getBytes())).thenReturn(Optional.empty());

        try {
            privacyGroupManager.retrievePrivacyGroup(groupId);
            failBecauseExceptionWasNotThrown(any());
        } catch (Exception ex) {
            assertThat(ex).isInstanceOf(PrivacyGroupNotFoundException.class);
        }

        verify(privacyGroupDAO).retrieve("id".getBytes());
    }

    @Test
    public void testStorePrivacyGroup() {

        final PrivacyGroup mockPrivacyGroup = mock(PrivacyGroup.class);
        when(mockPrivacyGroup.getPrivacyGroupId()).thenReturn(PublicKey.from("id".getBytes()));
        final byte[] encoded = "encoded".getBytes();
        when(privacyGroupUtil.decode(encoded)).thenReturn(mockPrivacyGroup);
        when(privacyGroupUtil.generateLookupId(anyList())).thenReturn("lookup".getBytes());

        privacyGroupManager.storePrivacyGroup(encoded);

        ArgumentCaptor<PrivacyGroupEntity> argCaptor = ArgumentCaptor.forClass(PrivacyGroupEntity.class);

        verify(privacyGroupDAO).save(argCaptor.capture());

        final PrivacyGroupEntity saved = argCaptor.getValue();

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo("id".getBytes());
        assertThat(saved.getLookupId()).isEqualTo("lookup".getBytes());
        assertThat(saved.getData()).isEqualTo("encoded".getBytes());

    }

    @Test
    public void create() {
        JdbcConfig jdbcConfig = new JdbcConfig("username", "password", "jdbc:h2:mem:test");
        final Config config = mock(Config.class);
        when(config.getJdbcConfig()).thenReturn(jdbcConfig);

        PrivacyGroupManager manager = PrivacyGroupManager.create(config);
        assertThat(manager).isNotNull();
    }
}
