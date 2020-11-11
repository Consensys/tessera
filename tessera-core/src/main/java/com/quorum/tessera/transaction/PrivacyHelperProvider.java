package com.quorum.tessera.transaction;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrivacyHelperProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrivacyHelperProvider.class);

    public static PrivacyHelper provider() {
        LOGGER.debug("Creating PrivacyHelper");
        Config config = ConfigFactory.create().getConfig();
        boolean privacyEnabled = config.getFeatures().isEnablePrivacyEnhancements();
        EncryptedTransactionDAO encryptedTransactionDAO = EncryptedTransactionDAO.create();
        PrivacyHelper privacyHelper = new PrivacyHelperImpl(encryptedTransactionDAO,privacyEnabled);
        LOGGER.debug("Created PrivacyHelper {}",privacyHelper);
        return privacyHelper;
    }

}
