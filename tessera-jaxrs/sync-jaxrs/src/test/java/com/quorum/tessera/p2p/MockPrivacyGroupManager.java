package com.quorum.tessera.p2p;

import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.privacygroup.PrivacyGroupManager;

import java.util.List;

public class MockPrivacyGroupManager implements PrivacyGroupManager {
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
