package com.quorum.tessera.privacygroup;

import com.quorum.tessera.data.privacygroup.PrivacyGroupDAO;
import com.quorum.tessera.data.privacygroup.PrivacyGroupEntity;
import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.enclave.PrivacyGroupUtil;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.privacygroup.publish.PrivacyGroupPublisher;

import java.util.List;
import java.util.stream.Collectors;

public class PrivacyGroupManagerImpl implements PrivacyGroupManager {

    private final PrivacyGroupDAO privacyGroupDAO;

    private final PrivacyGroupPublisher publisher;

    private final PrivacyGroupUtil privacyGroupUtil;

    public PrivacyGroupManagerImpl(PrivacyGroupDAO privacyGroupDAO, PrivacyGroupPublisher publisher) {
        this(privacyGroupDAO, publisher, PrivacyGroupUtil.create());
    }

    PrivacyGroupManagerImpl(PrivacyGroupDAO privacyGroupDAO, PrivacyGroupPublisher publisher, PrivacyGroupUtil privacyGroupUtil) {
        this.privacyGroupDAO = privacyGroupDAO;
        this.publisher = publisher;
        this.privacyGroupUtil = privacyGroupUtil;
    }

    @Override
    public PrivacyGroup createPrivacyGroup(String name, String description, List<PublicKey> members, byte[] seed) {

        final byte[] privacyGroupId = privacyGroupUtil.generatePrivacyGroupId(members, seed);

        final PrivacyGroup created =
                PrivacyGroup.Builder.create()
                        .withPrivacyGroupId(PublicKey.from(privacyGroupId))
                        .withName(name)
                        .withDescription(description)
                        .withMembers(members)
                        .withSeed(seed)
                        .withType(PrivacyGroup.Type.PANTHEON)
                        .withState(PrivacyGroup.State.ACTIVE)
                        .build();

        final byte[] lookupId = privacyGroupUtil.generateLookupId(members);

        final byte[] encodedData = privacyGroupUtil.encode(created);

        privacyGroupDAO.save(
                new PrivacyGroupEntity(privacyGroupId, lookupId, encodedData),
                () -> {
                    publisher.publishPrivacyGroup(encodedData, members);
                    return null;
                });

        return created;
    }

    @Override
    public PrivacyGroup createLegacyPrivacyGroup(List<PublicKey> members) {

        String name = "legacy";
        String description = "Privacy groups to support the creation of groups by privateFor and privateFrom";

        final byte[] privacyGroupId = privacyGroupUtil.generatePrivacyGroupId(members, null);

        final byte[] lookupId = privacyGroupUtil.generateLookupId(members);

        final PrivacyGroup created =
            PrivacyGroup.Builder.create()
                .withPrivacyGroupId(PublicKey.from(privacyGroupId))
                .withName(name)
                .withDescription(description)
                .withMembers(members)
                .withType(PrivacyGroup.Type.LEGACY)
                .withState(PrivacyGroup.State.ACTIVE)
                .build();

        final byte[] encodedData = privacyGroupUtil.encode(created);

        privacyGroupDAO.save(new PrivacyGroupEntity(privacyGroupId, lookupId, encodedData));

        return created;
    }

    @Override
    public List<PrivacyGroup> findPrivacyGroup(List<PublicKey> members) {

        final byte[] lookupId = privacyGroupUtil.generateLookupId(members);

        return privacyGroupDAO.findByLookupId(lookupId).stream()
            .map(PrivacyGroupEntity::getData)
            .map(privacyGroupUtil::decode)
            .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public PrivacyGroup retrievePrivacyGroup(PublicKey privacyGroupId) {
        return null;
    }

    @Override
    public void storePrivacyGroup(byte[] encodedData) {}
}
