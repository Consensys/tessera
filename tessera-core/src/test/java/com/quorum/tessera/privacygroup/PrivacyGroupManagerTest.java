package com.quorum.tessera.privacygroup;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.JdbcConfig;
import com.quorum.tessera.data.PrivacyGroupDAO;
import com.quorum.tessera.data.PrivacyGroupEntity;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.enclave.PrivacyGroupUtil;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.privacygroup.exception.PrivacyGroupNotFoundException;
import com.quorum.tessera.privacygroup.publish.BatchPrivacyGroupPublisher;
import com.quorum.tessera.transaction.exception.PrivacyViolationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PrivacyGroupManagerTest {

    private Enclave enclave;

    private PrivacyGroupManager privacyGroupManager;

    private PrivacyGroupDAO privacyGroupDAO;

    private PrivacyGroupUtil privacyGroupUtil;

    private BatchPrivacyGroupPublisher publisher;

    private final PublicKey localKey = PublicKey.from("ownKey".getBytes());

    @Before
    public void setUp() {
        enclave = mock(Enclave.class);
        privacyGroupDAO = mock(PrivacyGroupDAO.class);
        publisher = mock(BatchPrivacyGroupPublisher.class);
        privacyGroupUtil = mock(PrivacyGroupUtil.class);
        privacyGroupManager = new PrivacyGroupManagerImpl(enclave, privacyGroupDAO, publisher, privacyGroupUtil);

        when(enclave.getPublicKeys()).thenReturn(Set.of(localKey));
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(privacyGroupDAO, publisher);
    }

    @Test
    public void testCreatePrivacyGroup() {

        when(privacyGroupUtil.generateId(anyList(), any(byte[].class))).thenReturn("generatedId".getBytes());
        when(privacyGroupUtil.generateLookupId(anyList())).thenReturn("lookup".getBytes());
        when(privacyGroupUtil.encode(any())).thenReturn("encoded".getBytes());

        final List<PublicKey> members = List.of(localKey, mock(PublicKey.class), mock(PublicKey.class));

        doAnswer(
                        invocation -> {
                            Callable callable = invocation.getArgument(1);
                            callable.call();
                            return mock(PrivacyGroupEntity.class);
                        })
                .when(privacyGroupDAO)
                .save(any(), any());

        final PrivacyGroup privacyGroup =
                privacyGroupManager.createPrivacyGroup("name", "description", localKey, members, new byte[1]);

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

        List<PublicKey> forwardingMembers =
                members.stream().filter(Predicate.not(localKey::equals)).collect(Collectors.toList());

        assertThat(recipientsCaptor.getValue()).containsAll(forwardingMembers);

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
    public void testCreateFromKeyNotValid() {

        when(privacyGroupUtil.generateId(anyList(), any(byte[].class))).thenReturn("generatedId".getBytes());
        when(privacyGroupUtil.generateLookupId(anyList())).thenReturn("lookup".getBytes());
        when(privacyGroupUtil.encode(any())).thenReturn("encoded".getBytes());

        final List<PublicKey> members = List.of(mock(PublicKey.class), mock(PublicKey.class));

        assertThatThrownBy(
                        () ->
                                privacyGroupManager.createPrivacyGroup(
                                        "name", "description", localKey, members, new byte[1]))
                .isInstanceOf(PrivacyViolationException.class);
    }

    @Test
    public void testCreateLegacyPrivacyGroup() {

        final List<PublicKey> members = List.of(mock(PublicKey.class), mock(PublicKey.class));
        when(privacyGroupUtil.generateId(anyList())).thenReturn("generatedId".getBytes());
        when(privacyGroupUtil.generateLookupId(anyList())).thenReturn("lookup".getBytes());
        when(privacyGroupUtil.encode(any())).thenReturn("encoded".getBytes());
        when(privacyGroupDAO.retrieve("generatedId".getBytes())).thenReturn(Optional.empty());

        final PrivacyGroup privacyGroup = privacyGroupManager.createLegacyPrivacyGroup(localKey, members);

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
        assertThat(privacyGroup.getDescription())
                .isEqualTo("Privacy groups to support the creation of groups by privateFor and privateFrom");
        assertThat(privacyGroup.getMembers()).containsAll(members).contains(localKey);
        assertThat(privacyGroup.getType()).isEqualTo(PrivacyGroup.Type.LEGACY);
        assertThat(privacyGroup.getState()).isEqualTo(PrivacyGroup.State.ACTIVE);

        verify(privacyGroupDAO).retrieve("generatedId".getBytes());
    }

    @Test
    public void testLegacyPrivacyGroupExisted() {

        final List<PublicKey> members = List.of(localKey, mock(PublicKey.class), mock(PublicKey.class));
        when(privacyGroupUtil.generateId(anyList())).thenReturn("generatedId".getBytes());

        when(privacyGroupDAO.retrieve("generatedId".getBytes()))
                .thenReturn(Optional.of(mock(PrivacyGroupEntity.class)));

        final PrivacyGroup privacyGroup = privacyGroupManager.createLegacyPrivacyGroup(localKey, members);

        assertThat(privacyGroup).isNotNull();

        verify(privacyGroupDAO).retrieve("generatedId".getBytes());
    }

    @Test
    public void testFindPrivacyGroup() {

        final PrivacyGroupEntity et1 = mock(PrivacyGroupEntity.class);
        when(et1.getData()).thenReturn("data1".getBytes());
        final PrivacyGroupEntity et2 = mock(PrivacyGroupEntity.class);
        when(et2.getData()).thenReturn("data2".getBytes());
        final PrivacyGroupEntity et3 = mock(PrivacyGroupEntity.class);
        when(et3.getData()).thenReturn("data3".getBytes());
        final List<PrivacyGroupEntity> dbResult = List.of(et1, et2, et3);

        final PrivacyGroup pg1 = mock(PrivacyGroup.class);
        final PrivacyGroup pg2 = mock(PrivacyGroup.class);
        final PrivacyGroup pg3 = mock(PrivacyGroup.class);
        when(pg1.getState()).thenReturn(PrivacyGroup.State.DELETED);
        when(pg2.getState()).thenReturn(PrivacyGroup.State.ACTIVE);
        when(pg3.getState()).thenReturn(PrivacyGroup.State.ACTIVE);

        when(privacyGroupDAO.findByLookupId("lookup".getBytes())).thenReturn(dbResult);
        when(privacyGroupUtil.generateLookupId(anyList())).thenReturn("lookup".getBytes());
        when(privacyGroupUtil.decode("data1".getBytes())).thenReturn(pg1);
        when(privacyGroupUtil.decode("data2".getBytes())).thenReturn(pg2);
        when(privacyGroupUtil.decode("data3".getBytes())).thenReturn(pg3);

        final List<PrivacyGroup> privacyGroups = privacyGroupManager.findPrivacyGroup(List.of());

        assertThat(privacyGroups).isNotEmpty();
        assertThat(privacyGroups).contains(pg2, pg3);

        verify(privacyGroupDAO).findByLookupId("lookup".getBytes());
    }

    @Test
    public void testRetrievePrivacyGroup() {

        final PublicKey id = PublicKey.from("id".getBytes());
        final PrivacyGroupEntity mockResult = mock(PrivacyGroupEntity.class);
        final PrivacyGroup mockPrivacyGroup = mock(PrivacyGroup.class);
        when(mockPrivacyGroup.getState()).thenReturn(PrivacyGroup.State.ACTIVE);
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
    public void testRetrievePrivacyGroupDeleted() {

        final PublicKey id = PublicKey.from("id".getBytes());
        final PrivacyGroupEntity mockResult = mock(PrivacyGroupEntity.class);
        final PrivacyGroup mockPrivacyGroup = mock(PrivacyGroup.class);
        when(mockPrivacyGroup.getState()).thenReturn(PrivacyGroup.State.DELETED);
        when(mockResult.getData()).thenReturn("data".getBytes());

        when(privacyGroupUtil.decode("data".getBytes())).thenReturn(mockPrivacyGroup);

        when(privacyGroupDAO.retrieve("id".getBytes())).thenReturn(Optional.of(mockResult));

        try {
            privacyGroupManager.retrievePrivacyGroup(id);
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
        when(privacyGroupDAO.retrieve("id".getBytes())).thenReturn(Optional.empty());

        privacyGroupManager.storePrivacyGroup(encoded);

        ArgumentCaptor<PrivacyGroupEntity> argCaptor = ArgumentCaptor.forClass(PrivacyGroupEntity.class);

        verify(privacyGroupDAO).save(argCaptor.capture());
        verify(privacyGroupDAO).retrieve("id".getBytes());

        final PrivacyGroupEntity saved = argCaptor.getValue();

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo("id".getBytes());
        assertThat(saved.getLookupId()).isEqualTo("lookup".getBytes());
        assertThat(saved.getData()).isEqualTo("encoded".getBytes());
    }

    @Test
    public void testStoreUpdatedPrivacyGroup() {

        final PrivacyGroup mockPrivacyGroup = mock(PrivacyGroup.class);
        when(mockPrivacyGroup.getPrivacyGroupId()).thenReturn(PublicKey.from("id".getBytes()));
        when(mockPrivacyGroup.getState()).thenReturn(PrivacyGroup.State.DELETED);
        final byte[] encoded = "encoded".getBytes();
        when(privacyGroupUtil.decode(encoded)).thenReturn(mockPrivacyGroup);
        when(privacyGroupUtil.generateLookupId(anyList())).thenReturn("lookup".getBytes());
        when(privacyGroupDAO.retrieve("id".getBytes())).thenReturn(Optional.of(mock(PrivacyGroupEntity.class)));

        privacyGroupManager.storePrivacyGroup(encoded);

        ArgumentCaptor<PrivacyGroupEntity> argCaptor = ArgumentCaptor.forClass(PrivacyGroupEntity.class);

        verify(privacyGroupDAO).retrieve("id".getBytes());
        verify(privacyGroupDAO).update(argCaptor.capture());

        final PrivacyGroupEntity saved = argCaptor.getValue();

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo("id".getBytes());
        assertThat(saved.getLookupId()).isEqualTo("lookup".getBytes());
        assertThat(saved.getData()).isEqualTo("encoded".getBytes());
    }

    @Test
    public void testDeletePrivacyGroup() {

        PublicKey from = PublicKey.from("r1".getBytes());

        PrivacyGroupEntity retrievedEt = mock(PrivacyGroupEntity.class);
        when(retrievedEt.getData()).thenReturn("data".getBytes());
        PrivacyGroup mockPG = mock(PrivacyGroup.class);
        when(mockPG.getPrivacyGroupId()).thenReturn(PublicKey.from("id".getBytes()));
        when(mockPG.getMembers()).thenReturn(List.of(PublicKey.from("r1".getBytes()), PublicKey.from("r2".getBytes())));
        when(mockPG.getState()).thenReturn(PrivacyGroup.State.ACTIVE);
        when(mockPG.getType()).thenReturn(PrivacyGroup.Type.PANTHEON);

        when(privacyGroupDAO.retrieve("id".getBytes())).thenReturn(Optional.of(retrievedEt));

        when(privacyGroupUtil.decode("data".getBytes())).thenReturn(mockPG);
        when(privacyGroupUtil.encode(any())).thenReturn("deletedData".getBytes());
        when(privacyGroupUtil.generateLookupId(any())).thenReturn("lookup".getBytes());

        doAnswer(
                        invocation -> {
                            Callable callable = invocation.getArgument(1);
                            callable.call();
                            return mock(PrivacyGroupEntity.class);
                        })
                .when(privacyGroupDAO)
                .update(any(), any());

        PrivacyGroup result = privacyGroupManager.deletePrivacyGroup(from, PublicKey.from("id".getBytes()));

        assertThat(result.getState()).isEqualTo(PrivacyGroup.State.DELETED);

        verify(privacyGroupDAO).retrieve("id".getBytes());
        verify(privacyGroupDAO).update(any(), any());

        // Verify payload being distributed has the correct values
        ArgumentCaptor<byte[]> payloadCaptor = ArgumentCaptor.forClass(byte[].class);
        ArgumentCaptor<List<PublicKey>> recipientsCaptor = ArgumentCaptor.forClass(List.class);
        verify(publisher).publishPrivacyGroup(payloadCaptor.capture(), recipientsCaptor.capture());
        assertThat(payloadCaptor.getValue()).isEqualTo("deletedData".getBytes());

        assertThat(recipientsCaptor.getValue())
                .containsAll(List.of(PublicKey.from("r1".getBytes()), PublicKey.from("r2".getBytes())));

        ArgumentCaptor<PrivacyGroup> argCaptor = ArgumentCaptor.forClass(PrivacyGroup.class);
        verify(privacyGroupUtil).encode(argCaptor.capture());
        PrivacyGroup deletedPg = argCaptor.getValue();
        assertThat(deletedPg.getPrivacyGroupId()).isEqualTo(PublicKey.from("id".getBytes()));
        assertThat(deletedPg.getState()).isEqualTo(PrivacyGroup.State.DELETED);
    }

    @Test
    public void testDeletePrivacyGroupNotExist() {

        when(privacyGroupDAO.retrieve("id".getBytes())).thenReturn(Optional.empty());
        when(privacyGroupUtil.encode(any())).thenReturn("deletedData".getBytes());

        assertThatThrownBy(
                        () ->
                                privacyGroupManager.deletePrivacyGroup(
                                        mock(PublicKey.class), PublicKey.from("id".getBytes())))
                .isInstanceOf(PrivacyGroupNotFoundException.class);

        verify(privacyGroupDAO).retrieve("id".getBytes());
    }

    @Test
    public void testDeletePrivacyGroupFromKeyNotBelong() {

        PublicKey from = PublicKey.from("local".getBytes());

        PrivacyGroupEntity retrievedEt = mock(PrivacyGroupEntity.class);
        when(retrievedEt.getData()).thenReturn("data".getBytes());
        PrivacyGroup mockPG = mock(PrivacyGroup.class);
        when(mockPG.getPrivacyGroupId()).thenReturn(PublicKey.from("id".getBytes()));
        when(mockPG.getMembers()).thenReturn(List.of(PublicKey.from("r1".getBytes()), PublicKey.from("r2".getBytes())));
        when(mockPG.getState()).thenReturn(PrivacyGroup.State.ACTIVE);
        when(mockPG.getType()).thenReturn(PrivacyGroup.Type.PANTHEON);

        when(privacyGroupDAO.retrieve("id".getBytes())).thenReturn(Optional.of(retrievedEt));

        when(privacyGroupUtil.decode("data".getBytes())).thenReturn(mockPG);

        assertThatThrownBy(() -> privacyGroupManager.deletePrivacyGroup(from, PublicKey.from("id".getBytes())))
                .isInstanceOf(PrivacyViolationException.class);

        verify(privacyGroupDAO).retrieve("id".getBytes());
    }

    @Test
    public void testDeleteDeletedPrivacyGroup() {

        PrivacyGroupEntity retrievedEt = mock(PrivacyGroupEntity.class);
        when(retrievedEt.getData()).thenReturn("data".getBytes());
        PrivacyGroup mockPG = mock(PrivacyGroup.class);
        when(mockPG.getPrivacyGroupId()).thenReturn(PublicKey.from("id".getBytes()));
        when(mockPG.getMembers()).thenReturn(Collections.emptyList());
        when(mockPG.getState()).thenReturn(PrivacyGroup.State.DELETED);
        when(mockPG.getType()).thenReturn(PrivacyGroup.Type.PANTHEON);

        when(privacyGroupDAO.retrieve("id".getBytes())).thenReturn(Optional.of(retrievedEt));

        when(privacyGroupUtil.decode("data".getBytes())).thenReturn(mockPG);
        when(privacyGroupUtil.encode(any())).thenReturn("deletedData".getBytes());

        assertThatThrownBy(
                        () ->
                                privacyGroupManager.deletePrivacyGroup(
                                        mock(PublicKey.class), PublicKey.from("id".getBytes())))
                .isInstanceOf(PrivacyGroupNotFoundException.class);

        verify(privacyGroupDAO).retrieve("id".getBytes());
    }

    @Test
    public void defaultPublicKey() {
        privacyGroupManager.defaultPublicKey();
        verify(enclave).defaultPublicKey();
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
