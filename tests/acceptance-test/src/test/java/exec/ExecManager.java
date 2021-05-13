package exec;

import config.ConfigDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ExecManager {

  Logger LOG = LoggerFactory.getLogger(ExecManager.class);

  Process doStart() throws Exception;

  void doStop() throws Exception;

  default Process start() {
    try {
      return doStart();
    } catch (Exception ex) {
      LOG.error("", ex);
      throw new ExecException(ex);
    }
  }

  default void stop() {
    try {
      this.doStop();
    } catch (Exception ex) {
      LOG.warn("{}", ex.getMessage());
    }
  }

  ConfigDescriptor getConfigDescriptor();
}
