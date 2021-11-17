package com.quorum.tessera.config.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.Config;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import picocli.CommandLine;

@RunWith(Parameterized.class)
public class TesseraCommandTest {

  private Map config;

  public TesseraCommandTest(Map config) {
    this.config = config;
  }

  @Test
  public void unmatched() throws Exception {

    String[] args = new String[] {"--bogus=somevalue", "--anotherbogus"};

    CommandLine.ParseResult parseResult = new CommandLine(new TesseraCommand()).parseArgs(args);
    assertThat(parseResult.unmatched()).containsExactly(args);

    assertThat(parseResult.matchedArgs()).isEmpty();
  }

  @Test
  public void testArg() throws Exception {

    final String arg = String.class.cast(config.get("arg"));
    String[] tokens = arg.split("=");
    String name = tokens[0];

    Optional<String> value = tokens.length == 1 ? Optional.empty() : Optional.of(tokens[1]);

    CommandLine commandLine = new CommandLine(new TesseraCommand());
    Optional<CommandLine.ITypeConverter> converterOptional =
        (Optional<CommandLine.ITypeConverter>) config.get("convertor");
    converterOptional.ifPresent(
        c -> {
          Class type = (Class) ((Optional) config.get("convertorType")).get();
          commandLine.registerConverter(type, c);
        });

    CommandLine.ParseResult parseResult = commandLine.parseArgs(arg);

    assertThat(parseResult).isNotNull();
    assertThat(parseResult.hasMatchedOption(name))
        .describedAs("Should have option " + name)
        .isTrue();

    assertThat(parseResult.unmatched()).isEmpty();

    assertThat(parseResult.unmatched()).isEmpty();
    assertThat(parseResult.matchedArgs()).hasSize(1);

    if (converterOptional.isPresent()) {
      CommandLine.ITypeConverter converter = converterOptional.get();
      verify(converter).convert(value.get());
      verifyNoMoreInteractions(converter);
    }
  }

  @Parameterized.Parameters(name = "{0}")
  public static List<Map> configs() {
    return List.of(
        Map.of(
            "arg",
            "--pidfile=mypid",
            "convertor",
            Optional.of(mock(CommandLine.ITypeConverter.class)),
            "convertorType",
            Optional.of(Path.class)),
        Map.of(
            "arg", "-pidfile=mypid",
            "convertor", Optional.of(mock(CommandLine.ITypeConverter.class)),
            "convertorType", Optional.of(Path.class)),
        Map.of(
            "arg", "--configfile=myconfig.file",
            "convertor", Optional.of(mock(CommandLine.ITypeConverter.class)),
            "convertorType", Optional.of(Config.class)),
        Map.of(
            "arg", "-configfile=myconfig.file",
            "convertor", Optional.of(mock(CommandLine.ITypeConverter.class)),
            "convertorType", Optional.of(Config.class)),
        Map.of(
            "arg", "--config-file=myconfig.file",
            "convertor", Optional.of(mock(CommandLine.ITypeConverter.class)),
            "convertorType", Optional.of(Config.class)),
        Map.of(
            "arg", "--recover",
            "convertor", Optional.empty(),
            "convertorType", Optional.empty()),
        Map.of(
            "arg", "-r",
            "convertor", Optional.empty(),
            "convertorType", Optional.empty()),
        Map.of(
            "arg", "--override=foo=bar",
            "convertor", Optional.empty(),
            "convertorType", Optional.empty()));
  }
}
