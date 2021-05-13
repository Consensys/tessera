package suite;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerStatusCheckExecutor implements Callable<Boolean> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerStatusCheckExecutor.class);

  private final ServerStatusCheck serverStatusCheck;

  public ServerStatusCheckExecutor(ServerStatusCheck serverStatusCheck) {
    this.serverStatusCheck = serverStatusCheck;
  }

  @Override
  public Boolean call() throws Exception {
    LOGGER.info("Connecting {}", serverStatusCheck);
    do {
      LOGGER.debug("Unable to connect try again in 1 sec. {}", serverStatusCheck);
      TimeUnit.SECONDS.sleep(1);
    } while (!serverStatusCheck.checkStatus());

    LOGGER.info("Connected {}", serverStatusCheck);
    return true;
  }

  @Override
  public String toString() {
    return "ServerStatusCheckExecutor{" + "serverStatusCheck=" + serverStatusCheck + '}';
  }
}
