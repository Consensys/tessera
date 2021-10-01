package com.quorum.tessera.transaction.internal;

import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.transaction.PrivacyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrivacyHelperProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(PrivacyHelperProvider.class);

  public static PrivacyHelper provider() {
    RuntimeContext runtimeContext = RuntimeContext.getInstance();
    LOGGER.debug("Creating PrivacyHelper");
    boolean privacyEnabled = runtimeContext.isEnhancedPrivacy();
    EncryptedTransactionDAO encryptedTransactionDAO = EncryptedTransactionDAO.create();
    PrivacyHelper privacyHelper = new PrivacyHelperImpl(encryptedTransactionDAO, privacyEnabled);
    LOGGER.debug("Created PrivacyHelper {}", privacyHelper);
    return privacyHelper;
  }
}
