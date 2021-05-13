package com.quorum.tessera.config.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.Config;
import java.nio.file.Paths;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import picocli.CommandLine;

public class KeyGenFileUpdateOptionsTest {

  private CommandLine.ITypeConverter<Config> converter;

  private KeyGenFileUpdateOptions keyGenFileUpdateOptions = new KeyGenFileUpdateOptions();

  @Before
  public void beforeTest() {
    keyGenFileUpdateOptions = new KeyGenFileUpdateOptions();
    converter = mock(CommandLine.ITypeConverter.class);
  }

  @After
  public void afterTest() {
    verifyNoMoreInteractions(converter);
  }

  @Test
  public void configFileOnly() throws Exception {

    Config config = mock(Config.class);
    when(converter.convert(anyString())).thenReturn(config);

    CommandLine commandLine = new CommandLine(keyGenFileUpdateOptions);
    CommandLine.ParseResult result =
        commandLine.registerConverter(Config.class, converter).parseArgs("--configfile=myfile");

    assertThat(result).isNotNull();

    assertThat(keyGenFileUpdateOptions.getConfig()).isSameAs(config);

    verify(converter).convert("myfile");

    assertThat(keyGenFileUpdateOptions.getConfig()).isSameAs(config);
  }

  @Test
  public void configFileAndConfigout() throws Exception {

    Config config = mock(Config.class);
    when(converter.convert(anyString())).thenReturn(config);

    CommandLine commandLine = new CommandLine(keyGenFileUpdateOptions);
    CommandLine.ParseResult result =
        commandLine
            .registerConverter(Config.class, converter)
            .parseArgs("--configfile=myfile", "--configout=myconfigout");

    assertThat(result).isNotNull();
    verify(converter).convert("myfile");

    assertThat(keyGenFileUpdateOptions.getConfig()).isSameAs(config);

    assertThat(result.unmatched()).isEmpty();

    assertThat(result.matchedArgs()).hasSize(2);
    assertThat(result.hasMatchedOption("--configfile"));
    assertThat(result.hasMatchedOption("--configout"));
  }

  @Test
  public void configFileAndConfigoutAndPwout() throws Exception {

    Config config = mock(Config.class);
    when(converter.convert(anyString())).thenReturn(config);

    CommandLine commandLine = new CommandLine(keyGenFileUpdateOptions);
    CommandLine.ParseResult result =
        commandLine
            .registerConverter(Config.class, converter)
            .parseArgs("--configfile=myfile", "--configout=myconfigout", "--pwdout=mypwdout");

    assertThat(result).isNotNull();
    verify(converter).convert("myfile");

    assertThat(keyGenFileUpdateOptions.getConfig()).isSameAs(config);
    assertThat(keyGenFileUpdateOptions.getConfigOut()).isEqualTo(Paths.get("myconfigout"));
    assertThat(keyGenFileUpdateOptions.getPwdOut()).isEqualTo(Paths.get("mypwdout"));

    assertThat(result.unmatched()).isEmpty();

    assertThat(result.matchedArgs()).hasSize(3);
    assertThat(result.hasMatchedOption("--configfile")).isTrue();
    assertThat(result.hasMatchedOption("--configout")).isTrue();
    assertThat(result.hasMatchedOption("--pwdout")).isTrue();
  }

  @Test
  public void configFileAndPwout() throws Exception {

    Config config = mock(Config.class);
    when(converter.convert(anyString())).thenReturn(config);

    CommandLine commandLine = new CommandLine(keyGenFileUpdateOptions);
    CommandLine.ParseResult result =
        commandLine
            .registerConverter(Config.class, converter)
            .parseArgs("--configfile=myfile", "--pwdout=mypwdout");

    assertThat(result).isNotNull();
    verify(converter).convert("myfile");

    assertThat(keyGenFileUpdateOptions.getConfig()).isSameAs(config);

    assertThat(keyGenFileUpdateOptions.getPwdOut()).isEqualTo(Paths.get("mypwdout"));
    assertThat(keyGenFileUpdateOptions.getConfigOut()).isNull();

    assertThat(result.unmatched()).isEmpty();

    assertThat(result.matchedArgs()).hasSize(2);
    assertThat(result.hasMatchedOption("--configfile")).isTrue();
    assertThat(result.hasMatchedOption("--pwdout")).isTrue();
  }
}
