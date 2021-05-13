package com.quorum.tessera.config.cli;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.ArgonOptions;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;

public class ArgonOptionsConverterTest {

  private ArgonOptionsConverter argonOptionsConverter;

  @Before
  public void onSetUp() {
    argonOptionsConverter = new ArgonOptionsConverter();
  }

  @Test(expected = FileNotFoundException.class)
  public void fileNotFound() throws Exception {
    argonOptionsConverter.convert("path/to/nothing");
  }

  @Test
  public void fileContainsValidArgonJsonConfig() throws Exception {
    final String algorithm = "id";
    final Integer iterations = 10;
    final Integer memory = 10;
    final Integer parallelism = 10;

    final String config =
        String.format(
            "{\"variant\": \"%s\", \"iterations\":%s, \"memory\":%s, \"parallelism\":%s}",
            algorithm, iterations, memory, parallelism);

    final Path argonPath = Files.createTempFile(UUID.randomUUID().toString(), "");
    argonPath.toFile().deleteOnExit();

    Files.write(argonPath, config.getBytes());

    final ArgonOptions result = argonOptionsConverter.convert(argonPath.toString());

    final ArgonOptions expected = new ArgonOptions();
    expected.setAlgorithm(algorithm);
    expected.setIterations(iterations);
    expected.setMemory(memory);
    expected.setParallelism(parallelism);

    assertThat(result).isEqualToComparingFieldByField(expected);
  }
}
