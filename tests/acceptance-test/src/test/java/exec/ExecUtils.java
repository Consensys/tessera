package exec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class ExecUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecUtils.class);

    public static Process start(List<String> cmd, ExecutorService executorService, Map<String, String> env)
        throws IOException {

        LOGGER.info("Executing {}", String.join(" ", cmd));

        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        if (env != null) {
            processBuilder.environment().putAll(env);
        }
        processBuilder.redirectErrorStream(false);
        Process process = processBuilder.start();

        executorService.submit(new StreamConsumer(process.getErrorStream(),line -> LOGGER.error("Exec error data : {}", line)));
        executorService.submit(new StreamConsumer(process.getInputStream(),line -> LOGGER.debug("Exec line data : {}", line)));

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
