package com.quorum.tessera.cli.parsers;

import static com.quorum.tessera.test.util.ElUtil.createAndPopulatePaths;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.quorum.tessera.config.Config;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import org.junit.Test;

public class ConfigurationConverterTest {

  private ConfigConverter configConverter = new ConfigConverter();

  @Test
  public void configReadFromFile() throws Exception {
    final Path configFile = createAndPopulatePaths(getClass().getResource("/sample-config.json"));

    final Config result = configConverter.convert(configFile.toString());

    assertThat(result).isNotNull();
  }

  @Test
  public void configfileDoesNotExistThrowsException() {
    final String path = "does/not/exist.config";

    final Throwable throwable = catchThrowable(() -> configConverter.convert(path));

    assertThat(throwable)
        .isInstanceOf(FileNotFoundException.class)
        .hasMessage(path + " not found.");
  }
}
