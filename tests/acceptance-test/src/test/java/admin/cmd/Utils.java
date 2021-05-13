package admin.cmd;

import com.quorum.tessera.test.Party;
import exec.ExecArgsBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

  private static String jarPath = System.getProperty("application.jar");

  private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

  public static ExecutionResult start(Party party) throws IOException, InterruptedException {

    List<String> args =
        new ExecArgsBuilder()
            .withJvmArg(String.format("-Dnode.number=%S", party.getAlias()))
            .withStartScript(Paths.get(jarPath))
            .withConfigFile(party.getConfigFilePath())
            .build();

    ExecutorService executorService = Executors.newCachedThreadPool();

    ProcessBuilder processBuilder = new ProcessBuilder(args);

    processBuilder.redirectErrorStream(false);
    LOGGER.info("HERE Starting {}", String.join(",", args));
    Process process = processBuilder.start();

    ExecutionResult executionResult = new ExecutionResult();

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
              System.out.println(line);
              LOGGER.info(line);
              executionResult.addOutputLine(line);
            }

          } catch (IOException ex) {
            throw new UncheckedIOException(ex);
          }
        });

    executorService.submit(
        () -> {
          try (BufferedReader reader =
              Stream.of(process.getErrorStream())
                  .map(InputStreamReader::new)
                  .map(BufferedReader::new)
                  .findAny()
                  .get()) {

            String line = null;
            while ((line = reader.readLine()) != null) {
              LOGGER.error(line);
              executionResult.addErrorLine(line);
            }

          } catch (IOException ex) {
            throw new UncheckedIOException(ex);
          }
        });

    executionResult.setExitCode(process.waitFor());

    return executionResult;
  }

  public static int addPeer(Party party, String url) throws IOException, InterruptedException {

    List<String> args =
        new ExecArgsBuilder()
            .withJvmArg(String.format("-Dnode.number=%S", party.getAlias()))
            .withStartScript(Paths.get(jarPath))
            .withConfigFile(party.getConfigFilePath())
            .withSubcommands("admin", "addpeer")
            .withArg(url)
            .build();

    LOGGER.info("exec : {}", String.join(" ", args));
    ProcessBuilder processBuilder = new ProcessBuilder(args);
    processBuilder.redirectErrorStream(true);

    Process process = processBuilder.start();

    Collection<StreamConsumer> streamConsumers =
        Arrays.asList(
            new StreamConsumer(process.getErrorStream(), true),
            new StreamConsumer(process.getInputStream(), false));

    Executors.newCachedThreadPool().invokeAll(streamConsumers);

    return process.waitFor();
  }

  static class StreamConsumer implements Callable<Void> {

    private final InputStream inputStream;

    private boolean isError = false;

    StreamConsumer(InputStream inputStream, boolean isError) {
      this.inputStream = inputStream;
      this.isError = isError;
    }

    @Override
    public Void call() throws Exception {

      try (BufferedReader reader =
          Stream.of(inputStream)
              .map(InputStreamReader::new)
              .map(BufferedReader::new)
              .findAny()
              .get()) {

        String line = null;
        while ((line = reader.readLine()) != null) {
          if (isError) {
            LOGGER.error(line);
          } else {
            LOGGER.info(line);
          }
        }
        return null;
      }
    }
  }
}
