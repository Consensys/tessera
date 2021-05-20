package com.quorum.tessera.privacygroup.internal;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PrivacyGroupManagerImpl implements PrivacyGroupManager {

  private final Enclave enclave;

  private final PrivacyGroupDAO privacyGroupDAO;

  private final BatchPrivacyGroupPublisher publisher;

  private final PrivacyGroupUtil privacyGroupUtil;

  public PrivacyGroupManagerImpl(
      Enclave enclave, PrivacyGroupDAO privacyGroupDAO, BatchPrivacyGroupPublisher publisher) {
    this(enclave, privacyGroupDAO, publisher, PrivacyGroupUtil.create());
  }

  PrivacyGroupManagerImpl(
      Enclave enclave,
      PrivacyGroupDAO privacyGroupDAO,
      BatchPrivacyGroupPublisher publisher,
      PrivacyGroupUtil privacyGroupUtil) {
    this.enclave = enclave;
    this.privacyGroupDAO = privacyGroupDAO;
    this.publisher = publisher;
    this.privacyGroupUtil = privacyGroupUtil;
  }

  @Override
  public PrivacyGroup createPrivacyGroup(
      String name, String description, PublicKey from, List<PublicKey> members, byte[] seed) {

    final Set<PublicKey> localKeys = enclave.getPublicKeys();

    if (!members.contains(from)) {
      throw new PrivacyViolationException(
          "The list of members in a privacy group should include self");
    }

    final byte[] groupIdBytes = privacyGroupUtil.generateId(members, seed);

    final PrivacyGroup created =
        PrivacyGroup.Builder.create()
            .withPrivacyGroupId(groupIdBytes)
            .withName(name)
            .withDescription(description)
            .withMembers(members)
            .withSeed(seed)
            .withType(PrivacyGroup.Type.PANTHEON)
            .withState(PrivacyGroup.State.ACTIVE)
            .build();

    final byte[] lookupId = privacyGroupUtil.generateLookupId(members);
    final byte[] encodedData = privacyGroupUtil.encode(created);

    final List<PublicKey> forwardingMembers =
        members.stream().filter(Predicate.not(localKeys::contains)).collect(Collectors.toList());

    privacyGroupDAO.save(
        new PrivacyGroupEntity(groupIdBytes, lookupId, encodedData),
        () -> {
          publisher.publishPrivacyGroup(encodedData, forwardingMembers);
          return null;
        });

    return created;
  }

  @Override
  public PrivacyGroup createLegacyPrivacyGroup(PublicKey from, List<PublicKey> recipients) {

    final List<PublicKey> members = new ArrayList<>();
    members.add(from);
    members.addAll(recipients);

    final String name = "legacy";
    final String description =
        "Privacy groups to support the creation of groups by privateFor and privateFrom";

    final byte[] groupIdBytes = privacyGroupUtil.generateId(members);

    final PrivacyGroup created =
        PrivacyGroup.Builder.create()
            .withPrivacyGroupId(groupIdBytes)
            .withName(name)
            .withDescription(description)
            .withMembers(members)
            .withType(PrivacyGroup.Type.LEGACY)
            .withState(PrivacyGroup.State.ACTIVE)
            .build();

    final byte[] lookupId = privacyGroupUtil.generateLookupId(members);
    final byte[] encodedData = privacyGroupUtil.encode(created);

    privacyGroupDAO.retrieveOrSave(new PrivacyGroupEntity(groupIdBytes, lookupId, encodedData));

    return created;
  }

  @Override
  public PrivacyGroup saveResidentGroup(String name, String description, List<PublicKey> members) {

    final PrivacyGroup privacyGroup =
        PrivacyGroup.Builder.buildResidentGroup(name, description, members);

    final byte[] lookupId = privacyGroupUtil.generateLookupId(members);
    final byte[] encodedData = privacyGroupUtil.encode(privacyGroup);

    privacyGroupDAO.update(new PrivacyGroupEntity(name.getBytes(), lookupId, encodedData));

    return privacyGroup;
  }

  @Override
  public List<PrivacyGroup> findPrivacyGroup(List<PublicKey> members) {

    final byte[] lookupId = privacyGroupUtil.generateLookupId(members);

    return privacyGroupDAO.findByLookupId(lookupId).stream()
        .map(PrivacyGroupEntity::getData)
        .map(privacyGroupUtil::decode)
        .filter(pg -> pg.getState() == PrivacyGroup.State.ACTIVE)
        .collect(Collectors.toUnmodifiableList());
  }

  @Override
  public List<PrivacyGroup> findPrivacyGroupByType(PrivacyGroup.Type type) {
    return privacyGroupDAO.findAll().stream()
        .map(PrivacyGroupEntity::getData)
        .map(privacyGroupUtil::decode)
        .filter(pg -> pg.getState() == PrivacyGroup.State.ACTIVE && pg.getType() == type)
        .collect(Collectors.toUnmodifiableList());
  }

  @Override
  public PrivacyGroup retrievePrivacyGroup(PrivacyGroup.Id privacyGroupId) {

    return privacyGroupDAO
        .retrieve(privacyGroupId.getBytes())
        .map(PrivacyGroupEntity::getData)
        .map(privacyGroupUtil::decode)
        .filter(pg -> pg.getState() == PrivacyGroup.State.ACTIVE)
        .orElseThrow(
            () ->
                new PrivacyGroupNotFoundException(
                    "Privacy group " + privacyGroupId + " not found"));
  }

  @Override
  public void storePrivacyGroup(byte[] encodedData) {

    final PrivacyGroup privacyGroup = privacyGroupUtil.decode(encodedData);

    if (privacyGroup.getState() == PrivacyGroup.State.DELETED) {
      privacyGroupDAO
          .retrieve(privacyGroup.getId().getBytes())
          .ifPresent(
              et -> {
                et.setData(encodedData);
                privacyGroupDAO.update(et);
              });
      return;
    }
    final byte[] id = privacyGroup.getId().getBytes();
    final byte[] lookupId = privacyGroupUtil.generateLookupId(privacyGroup.getMembers());
    final PrivacyGroupEntity newEntity = new PrivacyGroupEntity(id, lookupId, encodedData);

    privacyGroupDAO.save(newEntity);
  }

  @Override
  public PrivacyGroup deletePrivacyGroup(PublicKey from, PrivacyGroup.Id privacyGroupId) {

    final PrivacyGroup retrieved = retrievePrivacyGroup(privacyGroupId);

    if (!retrieved.getMembers().contains(from)) {
      throw new PrivacyViolationException(
          "Sender of request does not belong to this privacy group");
    }

    final PrivacyGroup updated =
        PrivacyGroup.Builder.create().from(retrieved).withState(PrivacyGroup.State.DELETED).build();

    final byte[] updatedData = privacyGroupUtil.encode(updated);
    final byte[] lookupId = privacyGroupUtil.generateLookupId(updated.getMembers());
    final PrivacyGroupEntity updatedEt =
        new PrivacyGroupEntity(updated.getId().getBytes(), lookupId, updatedData);

    final Set<PublicKey> localKeys = enclave.getPublicKeys();
    final List<PublicKey> forwardingMembers =
        updated.getMembers().stream()
            .filter(Predicate.not(localKeys::contains))
            .collect(Collectors.toList());

    privacyGroupDAO.update(
        updatedEt,
        () -> {
          publisher.publishPrivacyGroup(updatedData, forwardingMembers);
          return null;
        });

    return updated;
  }

  @Override
  public PublicKey defaultPublicKey() {
    return enclave.defaultPublicKey();
  }

  @Override
  public Set<PublicKey> getManagedKeys() {
    return enclave.getPublicKeys();
  }
}
