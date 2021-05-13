package suite;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnixSocketServerStatusCheck implements ServerStatusCheck {

  private static final Logger LOGGER = LoggerFactory.getLogger(UnixSocketServerStatusCheck.class);

  private final File file;

  public UnixSocketServerStatusCheck(URI uri) {
    this.file = Paths.get(uri).toFile();
  }

  @Override
  public boolean checkStatus() {
    if (!Files.exists(file.toPath())) {
      LOGGER.debug("File does not exist yet {}", file);
      return false;
    }

    if (!file.canRead()) {
      LOGGER.debug("Cannot read file {} yet", file);
      return false;
    }

    if (!file.canWrite()) {
      LOGGER.debug("Cannot write to file {} yet", file);
      return false;
    }

    LOGGER.debug("File {} exists can get read and write.", file);
    UnixSocketAddress unixAddress = new UnixSocketAddress(file);
    LOGGER.debug("Open socket address {}", unixAddress);

    try (UnixSocketChannel channel = UnixSocketChannel.open(unixAddress)) {
      LOGGER.debug("Opened channel on socket address {}", unixAddress);
      return channel.isConnected();
    } catch (IOException ex) {
      LOGGER.debug("Exception connecting to {}. {}", unixAddress, ex.getMessage());
      return false;
    }
  }

  @Override
  public String toString() {
    return "UnixSocketServerStatusCheck{" + "file=" + file + '}';
  }
}
