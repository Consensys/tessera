package com.quorum.tessera.privacygroup;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.data.EntityManagerDAOFactory;
import com.quorum.tessera.data.PrivacyGroupDAO;
import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.privacygroup.exception.*;
import com.quorum.tessera.privacygroup.publish.PrivacyGroupPublisher;
import com.quorum.tessera.privacygroup.publish.PrivacyGroupPublisherFactory;

import java.util.List;

public interface PrivacyGroupManager {

    /**
     * Create a new privacy group
     *
     * @param name Name of the privacy group
     * @param description Description of the privacy group
     * @param members List of members public keys
     * @param seed Random seed used to generate privacy group id
     * @return Created privacy group object
     */
    PrivacyGroup createPrivacyGroup(String name, String description, List<PublicKey> members, byte[] seed);

    /**
     * Create a legacy privacy group to support EEA Transactions
     *
     * @param members List of members public keys
     * @return Created privacy group object
     */
    PrivacyGroup createLegacyPrivacyGroup(List<PublicKey> members);

    /**
     * Find the privacy groups in database based on its members
     *
     * @param members List of members public keys
     * @return A list of privacy groups associated with requested members
     */
    List<PrivacyGroup> findPrivacyGroup(List<PublicKey> members);

    /**
     * Retrieve the privacy group from database based on privacy group id. Throws an {@link
     * PrivacyGroupNotFoundException} if privacy group does not exist.
     *
     * @param privacyGroupId Privacy group id
     * @return Privacy group object
     */
    PrivacyGroup retrievePrivacyGroup(PublicKey privacyGroupId);

    /**
     * Store privacy group data from another node
     *
     * @param encodedData Encoded privacy group data
     */
    void storePrivacyGroup(byte[] encodedData);

    //    PublicKey deletePrivacyGroup(PublicKey privacyGroupId, PublicKey from);

    static PrivacyGroupManager create(final Config config) {
        return ServiceLoaderUtil.load(PrivacyGroupManager.class)
                .orElseGet(
                        () -> {
                            EntityManagerDAOFactory entityManagerDAOFactory =
                                    EntityManagerDAOFactory.newFactory(config);
                            PrivacyGroupDAO privacyGroupDAO = entityManagerDAOFactory.createPrivacyGroupDAO();
                            PrivacyGroupPublisher publisher =
                                    PrivacyGroupPublisherFactory.newFactory(config).create(config);
                            return new PrivacyGroupManagerImpl(privacyGroupDAO, publisher);
                        });
    }
}
