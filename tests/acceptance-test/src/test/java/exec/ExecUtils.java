package exec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecUtils.class);

    public static Process start(List<String> cmd, ExecutorService executorService, Map<String, String> env)
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

    public static void kill(String pid) throws IOException, InterruptedException {

        List<String> args = Arrays.asList("kill", pid);
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = processBuilder.start();

        int exitCode = process.waitFor();
    }
}
