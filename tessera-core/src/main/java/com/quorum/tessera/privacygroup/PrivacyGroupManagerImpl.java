package com.quorum.tessera.privacygroup;

import com.quorum.tessera.data.PrivacyGroupDAO;
import com.quorum.tessera.data.PrivacyGroupEntity;
import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.enclave.PrivacyGroupUtil;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.privacygroup.exception.PrivacyGroupNotFoundException;
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

    PrivacyGroupManagerImpl(
            PrivacyGroupDAO privacyGroupDAO, PrivacyGroupPublisher publisher, PrivacyGroupUtil privacyGroupUtil) {
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

        final String name = "legacy";
        final String description = "Privacy groups to support the creation of groups by privateFor and privateFrom";

        final byte[] privacyGroupId = privacyGroupUtil.generatePrivacyGroupId(members, null);

        final PrivacyGroup created =
                PrivacyGroup.Builder.create()
                        .withPrivacyGroupId(PublicKey.from(privacyGroupId))
                        .withName(name)
                        .withDescription(description)
                        .withMembers(members)
                        .withType(PrivacyGroup.Type.LEGACY)
                        .withState(PrivacyGroup.State.ACTIVE)
                        .build();

        if (privacyGroupDAO.retrieve(privacyGroupId).isPresent()) {
            return created;
        }

        final byte[] lookupId = privacyGroupUtil.generateLookupId(members);

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

        final byte[] id = privacyGroupId.getKeyBytes();

        final PrivacyGroupEntity result =
                privacyGroupDAO
                        .retrieve(id)
                        .orElseThrow(
                                () ->
                                        new PrivacyGroupNotFoundException(
                                                "Privacy group " + privacyGroupId + " not found"));

        final PrivacyGroup privacyGroup = privacyGroupUtil.decode(result.getData());

        return privacyGroup;
    }

    @Override
    public void storePrivacyGroup(byte[] encodedData) {

        final PrivacyGroup privacyGroup = privacyGroupUtil.decode(encodedData);

        final byte[] id = privacyGroup.getPrivacyGroupId().getKeyBytes();

        final byte[] lookupId = privacyGroupUtil.generateLookupId(privacyGroup.getMembers());

        final PrivacyGroupEntity entity = new PrivacyGroupEntity(id, lookupId, encodedData);

        privacyGroupDAO.save(entity);
    }

    //    @Override
    //    public PublicKey deletePrivacyGroup(PublicKey privacyGroupId, PublicKey from) {
    //        return null;
    //    }
}
