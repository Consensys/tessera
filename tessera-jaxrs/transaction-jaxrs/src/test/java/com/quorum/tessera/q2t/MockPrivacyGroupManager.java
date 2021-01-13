package com.quorum.tessera.q2t;

import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.privacygroup.PrivacyGroupManager;

import java.util.List;

public class MockPrivacyGroupManager implements PrivacyGroupManager {

    @Override
    public PrivacyGroup createPrivacyGroup(
            String name, String description, PublicKey from, List<PublicKey> members, byte[] seed) {
        return null;
    }

    @Override
    public PrivacyGroup createLegacyPrivacyGroup(PublicKey from, List<PublicKey> members) {
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
    public void storePrivacyGroup(byte[] encodedData) {}

    @Override
    public PrivacyGroup deletePrivacyGroup(PublicKey from, PublicKey privacyGroupId) {
        return null;
    }

    @Override
    public PublicKey defaultPublicKey() {
        return null;
    }
}
