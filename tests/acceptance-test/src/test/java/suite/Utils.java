package suite;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

public class Utils {

  public static URL toUrl(Path path) {

    try {
      return path.toUri().toURL();
    } catch (MalformedURLException ex) {
      throw new UncheckedIOException(ex);
    }
  }
}
