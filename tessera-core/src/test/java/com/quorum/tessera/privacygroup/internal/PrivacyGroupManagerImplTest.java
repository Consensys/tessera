package com.quorum.tessera.privacygroup.internal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.quorum.tessera.data.PrivacyGroupDAO;
import com.quorum.tessera.data.PrivacyGroupEntity;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.enclave.PrivacyGroupUtil;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.privacygroup.PrivacyGroupManager;
import com.quorum.tessera.privacygroup.exception.PrivacyGroupNotFoundException;
import com.quorum.tessera.privacygroup.publish.BatchPrivacyGroupPublisher;
import com.quorum.tessera.transaction.exception.PrivacyViolationException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class PrivacyGroupManagerImplTest {

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
    privacyGroupManager =
        new PrivacyGroupManagerImpl(enclave, privacyGroupDAO, publisher, privacyGroupUtil);

    when(enclave.getPublicKeys()).thenReturn(Set.of(localKey));
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(privacyGroupDAO, publisher);
  }

  @Test
  public void testCreatePrivacyGroup() {

    when(privacyGroupUtil.generateId(anyList(), any(byte[].class)))
        .thenReturn("generatedId".getBytes());
    when(privacyGroupUtil.generateLookupId(anyList())).thenReturn("lookup".getBytes());
    when(privacyGroupUtil.encode(any())).thenReturn("encoded".getBytes());
    PublicKey recipient1 = mock(PublicKey.class);
    PublicKey recipient2 = mock(PublicKey.class);
    final List<PublicKey> members = List.of(localKey, recipient1, recipient2);

    doAnswer(
            invocation -> {
              Callable callable = invocation.getArgument(1);
              callable.call();
              return mock(PrivacyGroupEntity.class);
            })
        .when(privacyGroupDAO)
        .save(any(), any());

    final PrivacyGroup privacyGroup =
        privacyGroupManager.createPrivacyGroup(
            "name", "description", localKey, members, new byte[1]);

    // Verify entity being saved has the correct values
    ArgumentCaptor<PrivacyGroupEntity> argCaptor =
        ArgumentCaptor.forClass(PrivacyGroupEntity.class);
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

    assertThat(recipientsCaptor.getValue()).containsExactlyInAnyOrder(recipient1, recipient2);

    // Verify generated privacy group has the correct values
    assertThat(privacyGroup).isNotNull();
    assertThat(privacyGroup.getId().getBytes()).isEqualTo("generatedId".getBytes());
    assertThat(privacyGroup.getName()).isEqualTo("name");
    assertThat(privacyGroup.getDescription()).isEqualTo("description");
    assertThat(privacyGroup.getMembers()).containsAll(members);
    assertThat(privacyGroup.getType()).isEqualTo(PrivacyGroup.Type.PANTHEON);
    assertThat(privacyGroup.getState()).isEqualTo(PrivacyGroup.State.ACTIVE);
  }

  @Test
  public void testCreateFromKeyNotValid() {

    when(privacyGroupUtil.generateId(anyList(), any(byte[].class)))
        .thenReturn("generatedId".getBytes());
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

    final PrivacyGroup privacyGroup =
        privacyGroupManager.createLegacyPrivacyGroup(localKey, members);

    // Verify entity being saved has the correct values
    ArgumentCaptor<PrivacyGroupEntity> argCaptor =
        ArgumentCaptor.forClass(PrivacyGroupEntity.class);
    verify(privacyGroupDAO).retrieveOrSave(argCaptor.capture());
    PrivacyGroupEntity savedEntity = argCaptor.getValue();
    assertThat(savedEntity).isNotNull();
    assertThat(savedEntity.getId()).isEqualTo("generatedId".getBytes());
    assertThat(savedEntity.getLookupId()).isEqualTo("lookup".getBytes());
    assertThat(savedEntity.getData()).isEqualTo("encoded".getBytes());

    // Verify generated privacy group has the correct values
    assertThat(privacyGroup).isNotNull();
    assertThat(privacyGroup.getId().getBytes()).isEqualTo("generatedId".getBytes());
    assertThat(privacyGroup.getName()).isEqualTo("legacy");
    assertThat(privacyGroup.getDescription())
        .isEqualTo(
            "Privacy groups to support the creation of groups by privateFor and privateFrom");
    assertThat(privacyGroup.getMembers()).containsAll(members).contains(localKey);
    assertThat(privacyGroup.getType()).isEqualTo(PrivacyGroup.Type.LEGACY);
    assertThat(privacyGroup.getState()).isEqualTo(PrivacyGroup.State.ACTIVE);

    verify(privacyGroupDAO).retrieveOrSave(any());
  }

  @Test
  public void testLegacyPrivacyGroupExisted() {

    final List<PublicKey> members = List.of(localKey, mock(PublicKey.class), mock(PublicKey.class));
    when(privacyGroupUtil.generateId(anyList())).thenReturn("generatedId".getBytes());

    when(privacyGroupDAO.retrieve("generatedId".getBytes()))
        .thenReturn(Optional.of(mock(PrivacyGroupEntity.class)));

    final PrivacyGroup privacyGroup =
        privacyGroupManager.createLegacyPrivacyGroup(localKey, members);

    assertThat(privacyGroup).isNotNull();

    verify(privacyGroupDAO).retrieveOrSave(any());
  }

  @Test
  public void testCreateResidentGroup() {
    when(privacyGroupUtil.generateLookupId(anyList())).thenReturn("lookup".getBytes());
    when(privacyGroupUtil.encode(any())).thenReturn("encoded".getBytes());
    when(privacyGroupDAO.retrieve("generatedId".getBytes())).thenReturn(Optional.empty());

    final PrivacyGroup privacyGroup =
        privacyGroupManager.saveResidentGroup("name", "desc", List.of(localKey));

    // Verify entity being saved has the correct values
    ArgumentCaptor<PrivacyGroupEntity> argCaptor =
        ArgumentCaptor.forClass(PrivacyGroupEntity.class);
    verify(privacyGroupDAO).update(argCaptor.capture());
    PrivacyGroupEntity savedEntity = argCaptor.getValue();
    assertThat(savedEntity).isNotNull();
    assertThat(savedEntity.getId()).isEqualTo("name".getBytes());
    assertThat(savedEntity.getLookupId()).isEqualTo("lookup".getBytes());
    assertThat(savedEntity.getData()).isEqualTo("encoded".getBytes());

    // Verify generated privacy group has the correct values
    assertThat(privacyGroup).isNotNull();
    assertThat(privacyGroup.getId().getBytes()).isEqualTo("name".getBytes());
    assertThat(privacyGroup.getName()).isEqualTo("name");
    assertThat(privacyGroup.getDescription()).isEqualTo("desc");
    assertThat(privacyGroup.getMembers()).containsExactly(localKey);
    assertThat(privacyGroup.getType()).isEqualTo(PrivacyGroup.Type.RESIDENT);
    assertThat(privacyGroup.getState()).isEqualTo(PrivacyGroup.State.ACTIVE);
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

    final PrivacyGroup.Id id = PrivacyGroup.Id.fromBytes("id".getBytes());
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

    final PrivacyGroup.Id groupId = PrivacyGroup.Id.fromBytes("id".getBytes());
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

    final PrivacyGroup.Id id = PrivacyGroup.Id.fromBytes("id".getBytes());
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
  public void findPrivacyGroupByType() {

    final PrivacyGroupEntity mockResult1 = mock(PrivacyGroupEntity.class);
    final PrivacyGroup mockPrivacyGroup1 = mock(PrivacyGroup.class);
    when(mockPrivacyGroup1.getState()).thenReturn(PrivacyGroup.State.ACTIVE);
    when(mockPrivacyGroup1.getType()).thenReturn(PrivacyGroup.Type.RESIDENT);
    when(mockResult1.getData()).thenReturn("data1".getBytes());

    final PrivacyGroupEntity mockResult2 = mock(PrivacyGroupEntity.class);
    final PrivacyGroup mockPrivacyGroup2 = mock(PrivacyGroup.class);
    when(mockPrivacyGroup2.getState()).thenReturn(PrivacyGroup.State.DELETED);
    when(mockPrivacyGroup2.getType()).thenReturn(PrivacyGroup.Type.RESIDENT);
    when(mockResult2.getData()).thenReturn("data2".getBytes());

    final PrivacyGroupEntity mockResult3 = mock(PrivacyGroupEntity.class);
    final PrivacyGroup mockPrivacyGroup3 = mock(PrivacyGroup.class);
    when(mockPrivacyGroup3.getState()).thenReturn(PrivacyGroup.State.ACTIVE);
    when(mockPrivacyGroup3.getType()).thenReturn(PrivacyGroup.Type.PANTHEON);
    when(mockResult3.getData()).thenReturn("data3".getBytes());

    when(privacyGroupUtil.decode("data1".getBytes())).thenReturn(mockPrivacyGroup1);
    when(privacyGroupUtil.decode("data2".getBytes())).thenReturn(mockPrivacyGroup2);
    when(privacyGroupUtil.decode("data3".getBytes())).thenReturn(mockPrivacyGroup3);

    when(privacyGroupDAO.findAll()).thenReturn(List.of(mockResult1, mockResult2, mockResult3));

    final List<PrivacyGroup> result =
        privacyGroupManager.findPrivacyGroupByType(PrivacyGroup.Type.RESIDENT);

    assertThat(result).isNotNull();
    assertThat(result).containsExactly(mockPrivacyGroup1);

    verify(privacyGroupDAO).findAll();
  }

  @Test
  public void testStorePrivacyGroup() {

    final PrivacyGroup mockPrivacyGroup = mock(PrivacyGroup.class);
    when(mockPrivacyGroup.getId()).thenReturn(PrivacyGroup.Id.fromBytes("id".getBytes()));
    final byte[] encoded = "encoded".getBytes();
    when(privacyGroupUtil.decode(encoded)).thenReturn(mockPrivacyGroup);
    when(privacyGroupUtil.generateLookupId(anyList())).thenReturn("lookup".getBytes());
    when(privacyGroupDAO.retrieve("id".getBytes())).thenReturn(Optional.empty());

    privacyGroupManager.storePrivacyGroup(encoded);

    ArgumentCaptor<PrivacyGroupEntity> argCaptor =
        ArgumentCaptor.forClass(PrivacyGroupEntity.class);

    verify(privacyGroupDAO).save(argCaptor.capture());

    final PrivacyGroupEntity saved = argCaptor.getValue();

    assertThat(saved).isNotNull();
    assertThat(saved.getId()).isEqualTo("id".getBytes());
    assertThat(saved.getLookupId()).isEqualTo("lookup".getBytes());
    assertThat(saved.getData()).isEqualTo("encoded".getBytes());
  }

  @Test
  public void testStoreUpdatedPrivacyGroup() {

    final PrivacyGroup mockPrivacyGroup = mock(PrivacyGroup.class);
    when(mockPrivacyGroup.getId()).thenReturn(PrivacyGroup.Id.fromBytes("id".getBytes()));
    when(mockPrivacyGroup.getState()).thenReturn(PrivacyGroup.State.DELETED);
    final byte[] encoded = "encoded".getBytes();
    when(privacyGroupUtil.decode(encoded)).thenReturn(mockPrivacyGroup);
    when(privacyGroupUtil.generateLookupId(anyList())).thenReturn("lookup".getBytes());
    PrivacyGroupEntity existing =
        new PrivacyGroupEntity("id".getBytes(), "lookup".getBytes(), "old".getBytes());
    when(privacyGroupDAO.retrieve("id".getBytes())).thenReturn(Optional.of(existing));

    privacyGroupManager.storePrivacyGroup(encoded);

    ArgumentCaptor<PrivacyGroupEntity> argCaptor =
        ArgumentCaptor.forClass(PrivacyGroupEntity.class);

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
    when(mockPG.getId()).thenReturn(PrivacyGroup.Id.fromBytes("id".getBytes()));
    when(mockPG.getMembers())
        .thenReturn(List.of(PublicKey.from("r1".getBytes()), PublicKey.from("r2".getBytes())));
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

    PrivacyGroup result =
        privacyGroupManager.deletePrivacyGroup(from, PrivacyGroup.Id.fromBytes("id".getBytes()));

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
    assertThat(deletedPg.getId()).isEqualTo(PrivacyGroup.Id.fromBytes("id".getBytes()));
    assertThat(deletedPg.getState()).isEqualTo(PrivacyGroup.State.DELETED);
  }

  @Test
  public void testDeletePrivacyGroupNotExist() {

    when(privacyGroupDAO.retrieve("id".getBytes())).thenReturn(Optional.empty());
    when(privacyGroupUtil.encode(any())).thenReturn("deletedData".getBytes());

    assertThatThrownBy(
            () ->
                privacyGroupManager.deletePrivacyGroup(
                    mock(PublicKey.class), PrivacyGroup.Id.fromBytes("id".getBytes())))
        .isInstanceOf(PrivacyGroupNotFoundException.class);

    verify(privacyGroupDAO).retrieve("id".getBytes());
  }

  @Test
  public void testDeletePrivacyGroupFromKeyNotBelong() {

    PublicKey from = PublicKey.from("local".getBytes());

    PrivacyGroupEntity retrievedEt = mock(PrivacyGroupEntity.class);
    when(retrievedEt.getData()).thenReturn("data".getBytes());
    PrivacyGroup mockPG = mock(PrivacyGroup.class);
    when(mockPG.getId()).thenReturn(PrivacyGroup.Id.fromBytes("id".getBytes()));
    when(mockPG.getMembers())
        .thenReturn(List.of(PublicKey.from("r1".getBytes()), PublicKey.from("r2".getBytes())));
    when(mockPG.getState()).thenReturn(PrivacyGroup.State.ACTIVE);
    when(mockPG.getType()).thenReturn(PrivacyGroup.Type.PANTHEON);

    when(privacyGroupDAO.retrieve("id".getBytes())).thenReturn(Optional.of(retrievedEt));

    when(privacyGroupUtil.decode("data".getBytes())).thenReturn(mockPG);

    assertThatThrownBy(
            () ->
                privacyGroupManager.deletePrivacyGroup(
                    from, PrivacyGroup.Id.fromBytes("id".getBytes())))
        .isInstanceOf(PrivacyViolationException.class);

    verify(privacyGroupDAO).retrieve("id".getBytes());
  }

  @Test
  public void testDeleteDeletedPrivacyGroup() {

    PrivacyGroupEntity retrievedEt = mock(PrivacyGroupEntity.class);
    when(retrievedEt.getData()).thenReturn("data".getBytes());
    PrivacyGroup mockPG = mock(PrivacyGroup.class);
    when(mockPG.getId()).thenReturn(PrivacyGroup.Id.fromBytes("id".getBytes()));
    when(mockPG.getMembers()).thenReturn(Collections.emptyList());
    when(mockPG.getState()).thenReturn(PrivacyGroup.State.DELETED);
    when(mockPG.getType()).thenReturn(PrivacyGroup.Type.PANTHEON);

    when(privacyGroupDAO.retrieve("id".getBytes())).thenReturn(Optional.of(retrievedEt));

    when(privacyGroupUtil.decode("data".getBytes())).thenReturn(mockPG);
    when(privacyGroupUtil.encode(any())).thenReturn("deletedData".getBytes());

    assertThatThrownBy(
            () ->
                privacyGroupManager.deletePrivacyGroup(
                    mock(PublicKey.class), PrivacyGroup.Id.fromBytes("id".getBytes())))
        .isInstanceOf(PrivacyGroupNotFoundException.class);

    verify(privacyGroupDAO).retrieve("id".getBytes());
  }

  @Test
  public void defaultPublicKey() {
    privacyGroupManager.defaultPublicKey();
    verify(enclave).defaultPublicKey();
  }

  @Test
  public void managedKeys() {
    privacyGroupManager.getManagedKeys();
    verify(enclave).getPublicKeys();
  }
}
