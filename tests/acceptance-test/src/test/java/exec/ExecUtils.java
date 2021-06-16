package exec;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExecUtils.class);

  public static Process start(
      List<String> cmd, ExecutorService executorService, Map<String, String> env)
      throws IOException {

    LOGGER.info("Executing {}", String.join(" ", cmd));

    ProcessBuilder processBuilder = new ProcessBuilder(cmd);
    if (env != null) {
      processBuilder.environment().putAll(env);
    }
    processBuilder.redirectErrorStream(false);
    Process process = processBuilder.start();

    executorService.submit(
        new StreamConsumer(
            process.getErrorStream(), line -> LOGGER.error("Exec error data : {}", line)));
    executorService.submit(
        new StreamConsumer(
            process.getInputStream(), line -> LOGGER.debug("Exec line data : {}", line)));

    executorService.submit(
        () -> {
          try {
            int exitCode = process.waitFor();
            LOGGER.info("Exec exit code: {}", exitCode);
          } catch (InterruptedException ex) {
            LOGGER.warn(ex.getMessage());
          }
        });

    return process;
  }

  public static void kill(Path pidFile) {
    Stream.of(pidFile)
        .filter(Files::exists)
        .flatMap(
            p -> {
              try {
                return Files.lines(p);
              } catch (IOException e) {
                LOGGER.debug(null, e);
                throw new UncheckedIOException(e);
              }
            })
        .findFirst()
        .ifPresent(ExecUtils::kill);
  }

  public static void kill(String pid) {

    List<String> args = List.of("kill", pid);
    ProcessBuilder processBuilder = new ProcessBuilder(args);
    try {
      Process process = processBuilder.start();
      int exitCode = process.waitFor();
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    } catch (InterruptedException ex) {
      LOGGER.warn("", ex);
    }
  }
}
