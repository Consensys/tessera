package exec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
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
    processBuilder.redirectErrorStream(true);
    Process process = processBuilder.start();

    executorService.submit(
        () -> {
          try (BufferedReader reader =
              Stream.of(process.getInputStream())
                  .map(InputStreamReader::new)
                  .map(BufferedReader::new)
                  .findAny()
                  .get()) {

            String line = null;
            while ((line = reader.readLine()) != null) {
              LOGGER.debug("Exec : {}", line);
            }
          }
          return null;
        });

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

  public static void kill(String pid) {

    Optional<ProcessHandle> optionalProcessHandle = ProcessHandle.of(Long.valueOf(pid));
    try {
      ProcessHandle processHandle = optionalProcessHandle.get();
      LOGGER.debug("Killing process, pid: {}", processHandle.pid());
      processHandle.destroy();

      for (int i = 0; i < 10; i++) {
        if (processHandle.isAlive()) {
          LOGGER.debug("Waiting for process to exit, pid: {}", processHandle.pid());
          try {
            Thread.sleep(100L);
          } catch (InterruptedException ex) {
          }
        } else {
          LOGGER.debug("Process successfully killed, pid: {}", processHandle.pid());
          return;
        }
      }
    } catch (NoSuchElementException e) {
      LOGGER.debug("No such process, pid: {}", pid);
    }
    LOGGER.warn("Process did not exit yet, pid: {}", pid);
  }
}
