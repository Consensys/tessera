package net.consensys.tessera.migration.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.consensys.orion.config.Config;
import org.junit.After;
import org.junit.Test;

public class InboundDbHelperTest {

  @After
  public void afterTest() throws Exception {
    Path storageDir = Paths.get("build", "junitdb");
    if (Files.notExists(storageDir)) {
      return;
    }

    Files.list(storageDir)
        .forEach(
            p -> {
              try {
                System.out.println(p);
                Files.deleteIfExists(p);
              } catch (IOException e) {
                throw new UncheckedIOException(e);
              }
            });

    Files.deleteIfExists(storageDir);
  }

  @Test
  public void leveldb() throws IOException {

    Path storageDir = Paths.get("build", "junitdb");
    Files.createDirectories(storageDir);
    Files.createFile(storageDir.resolve("LOCK"));

    Config leveldbConfig = mock(Config.class);
    when(leveldbConfig.storage()).thenReturn("leveldb:junitdb");
    when(leveldbConfig.workDir()).thenReturn(Paths.get("build"));

    InboundDbHelper inboundDbHelper = InboundDbHelper.from(leveldbConfig);
    assertThat(inboundDbHelper.getInputType()).isEqualTo(InputType.LEVELDB);
    assertThat(inboundDbHelper.getLevelDb()).isPresent();
    assertThat(inboundDbHelper.getJdbcDataSource()).isNotPresent();
  }

  @Test
  public void jdbc() {
    Config jdbcConfig = mock(Config.class);
    when(jdbcConfig.storage()).thenReturn("sql:jdbc:h2:mem:junit");
    when(jdbcConfig.workDir()).thenReturn(Paths.get("build"));

    InboundDbHelper inboundDbHelper = InboundDbHelper.from(jdbcConfig);
    assertThat(inboundDbHelper.getInputType()).isEqualTo(InputType.JDBC);
    assertThat(inboundDbHelper.getLevelDb()).isNotPresent();
    assertThat(inboundDbHelper.getJdbcDataSource()).isPresent();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void unsupported() {
    Config unsupportedConfig = mock(Config.class);
    when(unsupportedConfig.storage()).thenReturn("unsupported");
    when(unsupportedConfig.workDir()).thenReturn(Paths.get("build"));

    InboundDbHelper.from(unsupportedConfig);
  }
}
