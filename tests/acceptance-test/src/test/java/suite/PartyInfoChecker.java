package suite;

import com.quorum.tessera.config.CommunicationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface PartyInfoChecker {

  Logger LOGGER = LoggerFactory.getLogger(PartyInfoChecker.class);

  boolean hasSynced();

  static PartyInfoChecker create(CommunicationType communicationType) {
    LOGGER.info("Creating checker for {}", communicationType);
    return new RestPartyInfoChecker();
  }
}
