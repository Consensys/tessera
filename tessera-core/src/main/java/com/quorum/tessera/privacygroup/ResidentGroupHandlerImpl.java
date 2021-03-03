package com.quorum.tessera.privacygroup;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.encryption.PublicKey;
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

        final List<PrivacyGroup> configured =
                Stream.ofNullable(config.getResidentGroups())
                        .flatMap(Collection::stream)
                        .map(
                                rg -> {
                                    final List<PublicKey> members =
                                            rg.getMembers().stream()
                                                    .map(Base64.getDecoder()::decode)
                                                    .map(PublicKey::from)
                                                    .collect(Collectors.toList());
                                    return PrivacyGroup.Builder.buildResidentGroup(
                                            rg.getName(), rg.getDescription(), members);
                                })
                        .collect(Collectors.toUnmodifiableList());

        final Set<PublicKey> residentList =
                configured.stream()
                        .map(PrivacyGroup::getMembers)
                        .flatMap(List::stream)
                        .collect(Collectors.toUnmodifiableSet());

        final Set<PublicKey> managedKeys = privacyGroupManager.getManagedKeys();

        if (!managedKeys.containsAll(residentList)) {
            throw new PrivacyViolationException("Keys configured in resident groups need to be locally managed");
        }

        final List<PublicKey> homelessKeys =
                managedKeys.stream()
                        .filter(Predicate.not(residentList::contains))
                        .collect(Collectors.toUnmodifiableList());
        final PrivacyGroup defaultPrivateGroup =
                PrivacyGroup.Builder.buildResidentGroup("private", "Default private resident group", homelessKeys);

        final List<PrivacyGroup> existing = privacyGroupManager.findPrivacyGroupByType(PrivacyGroup.Type.RESIDENT);

        final List<PrivacyGroup> allResidentGroups = new ArrayList<>(configured);
        allResidentGroups.addAll(existing);
        allResidentGroups.add(defaultPrivateGroup);

        final List<PrivacyGroup> merged =
                allResidentGroups.stream()
                        .collect(
                                Collectors.collectingAndThen(
                                        Collectors.toMap(
                                                PrivacyGroup::getId,
                                                Function.identity(),
                                                (left, right) -> {
                                                    final List<PublicKey> mergedMembers =
                                                            Stream.concat(
                                                                            left.getMembers().stream(),
                                                                            right.getMembers().stream())
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
            throw new PrivacyViolationException("A local owned key cannot belong to more than one resident group");
        }

        final List<PrivacyGroup.Id> configuredGroupId =
                Stream.concat(configured.stream(), Stream.of(defaultPrivateGroup))
                        .map(PrivacyGroup::getId)
                        .collect(Collectors.toList());

        merged.stream()
                .filter(pg -> configuredGroupId.contains(pg.getId()))
                .collect(Collectors.toList())
                .forEach(
                        toPersist ->
                                privacyGroupManager.saveResidentGroup(
                                        toPersist.getName(), toPersist.getDescription(), toPersist.getMembers()));
    }
}
