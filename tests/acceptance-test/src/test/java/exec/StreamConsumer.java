package exec;

import java.io.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class StreamConsumer implements Runnable {

  private InputStream inputStream;

  private final Consumer<String> lineConsumer;

  public StreamConsumer(InputStream inputStream) {
    this(inputStream, (line) -> System.out.println("LINEOUT " + line));
  }

  public StreamConsumer(InputStream inputStream, Consumer<String> lineConsumer) {
    this.inputStream = inputStream;
    this.lineConsumer = lineConsumer;
  }

  @Override
  public void run() {
    try (BufferedReader reader =
        Stream.of(inputStream)
            .map(InputStreamReader::new)
            .map(BufferedReader::new)
            .findAny()
            .get()) {

      String line;
      while ((line = reader.readLine()) != null) {
        lineConsumer.accept(line);
      }

    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }
}
