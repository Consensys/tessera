package com.quorum.tessera.privacygroup;

import com.quorum.tessera.data.privacygroup.PrivacyGroupDAO;
import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.enclave.PrivacyGroupUtil;
import com.quorum.tessera.encryption.PublicKey;

import java.util.List;

public class PrivacyGroupManagerImpl implements PrivacyGroupManager {

    private final PrivacyGroupDAO privacyGroupDAO;

    private final PrivacyGroupUtil privacyGroupUtil;

    public PrivacyGroupManagerImpl(final PrivacyGroupDAO privacyGroupDAO) {
        this.privacyGroupDAO = privacyGroupDAO;
        this.privacyGroupUtil = PrivacyGroupUtil.create();
    }

    @Override
    public PrivacyGroup createPrivacyGroup(String name, String description, List<PublicKey> members, byte[] seed) {
        return null;
    }

    @Override
    public PrivacyGroup createLegacyPrivacyGroup(List<PublicKey> members) {
        return null;
    }

    @Override
    public List<PrivacyGroup> findPrivacyGroup(List<PublicKey> members) {
        return null;
    }

    @Override
    public PrivacyGroup retrievePrivacyGroup(PublicKey privacyGroupId) {
        return null;
    }

    @Override
    public void storePrivacyGroup(byte[] encodedData) {

    }
}
