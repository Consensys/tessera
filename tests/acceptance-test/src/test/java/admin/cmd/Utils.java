package admin.cmd;

import com.quorum.tessera.test.Party;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

    private static String jarPath = System.getProperty("application.jar", "../../tessera-app/target/tessera-app-0.7-SNAPSHOT-app.jar");

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    public static int addPeer(Party party, String url) throws IOException, InterruptedException {
  
        List<String> args = Arrays.asList(
            "java",
            "-jar",
            jarPath,
            "-configfile",
            party.getConfigFilePath().toAbsolutePath().toString(),
            "admin",
            "-addpeer",
            url
        );

        LOGGER.info("exec : {}", String.join(" ", args));
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        Collection<StreamConsumer> streamConsumers = Arrays.asList(
            new StreamConsumer(process.getErrorStream(), true),
            new StreamConsumer(process.getInputStream(), false)
        );

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

            try (BufferedReader reader = Stream.of(inputStream)
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
