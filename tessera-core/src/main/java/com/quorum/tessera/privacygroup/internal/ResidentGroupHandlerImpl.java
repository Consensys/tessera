package com.quorum.tessera.privacygroup.internal;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ResidentGroup;
import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.privacygroup.PrivacyGroupManager;
import com.quorum.tessera.privacygroup.ResidentGroupHandler;
import com.quorum.tessera.transaction.exception.PrivacyViolationException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResidentGroupHandlerImpl implements ResidentGroupHandler {

  private final PrivacyGroupManager privacyGroupManager;

  public ResidentGroupHandlerImpl(PrivacyGroupManager privacyGroupManager) {
    this.privacyGroupManager = privacyGroupManager;
  }

  @Override
  public void onCreate(Config config) {

    final Set<PublicKey> managedKeys = privacyGroupManager.getManagedKeys();

    final List<PrivacyGroup> configuredResidentGroups =
        Stream.ofNullable(config.getResidentGroups())
            .flatMap(Collection::stream)
            .map(convertToPrivacyGroup)
            .collect(Collectors.toUnmodifiableList());

    configuredResidentGroups.stream()
        .map(PrivacyGroup::getMembers)
        .flatMap(List::stream)
        .filter(Predicate.not(managedKeys::contains))
        .findFirst()
        .ifPresent(
            key -> {
              throw new PrivacyViolationException(
                  "Key " + key + " configured in resident groups must be locally managed");
            });

    final List<PrivacyGroup> existing =
        privacyGroupManager.findPrivacyGroupByType(PrivacyGroup.Type.RESIDENT);

    final List<PrivacyGroup> allResidentGroups = new ArrayList<>(configuredResidentGroups);
    allResidentGroups.addAll(existing);

    final List<PrivacyGroup> merged =
        allResidentGroups.stream()
            .collect(
                Collectors.collectingAndThen(
                    Collectors.toMap(
                        PrivacyGroup::getId,
                        Function.identity(),
                        (left, right) -> {
                          final List<PublicKey> mergedMembers =
                              Stream.concat(left.getMembers().stream(), right.getMembers().stream())
                                  .distinct()
                                  .collect(Collectors.toUnmodifiableList());
                          return PrivacyGroup.Builder.create()
                              .from(left)
                              .withMembers(mergedMembers)
                              .build();
                        }),
                    m -> new ArrayList<>(m.values())));

    try {
      merged.stream()
          .flatMap(p -> p.getMembers().stream().distinct().map(m -> Map.entry(m, p.getId())))
          .distinct()
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    } catch (IllegalStateException ex) {
      throw new PrivacyViolationException(
          "Key cannot belong to more than one resident group." + "Cause: " + ex.getMessage());
    }

    final Set<PublicKey> mergedResidentKeys =
        merged.stream()
            .map(PrivacyGroup::getMembers)
            .flatMap(List::stream)
            .collect(Collectors.toUnmodifiableSet());

    managedKeys.stream()
        .filter(Predicate.not(mergedResidentKeys::contains))
        .findAny()
        .ifPresent(
            key -> {
              throw new PrivacyViolationException(key + " must belong to a resident group");
            });

    final List<PrivacyGroup.Id> configuredGroupId =
        configuredResidentGroups.stream().map(PrivacyGroup::getId).collect(Collectors.toList());

    merged.stream()
        .filter(pg -> configuredGroupId.contains(pg.getId()))
        .collect(Collectors.toList())
        .forEach(
            toPersist ->
                privacyGroupManager.saveResidentGroup(
                    toPersist.getName(), toPersist.getDescription(), toPersist.getMembers()));
  }

  private Function<ResidentGroup, PrivacyGroup> convertToPrivacyGroup =
      group -> {
        final List<PublicKey> members =
            group.getMembers().stream()
                .map(Base64.getDecoder()::decode)
                .map(PublicKey::from)
                .collect(Collectors.toList());
        return PrivacyGroup.Builder.buildResidentGroup(
            group.getName(), group.getDescription(), members);
      };
}
