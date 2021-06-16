package com.quorum.tessera.test.cli.version;

import static org.assertj.core.api.Assertions.assertThat;

import exec.ExecArgsBuilder;
import io.cucumber.java8.En;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionSteps implements En {

  private static final URL logbackConfigFile = VersionSteps.class.getResource("/logback-node.xml");

  private static final Logger LOGGER = LoggerFactory.getLogger(VersionSteps.class);

  private Process process;

  private String capturedVersion;

  public VersionSteps() {
    When(
        "^Tessera is started with \"([^\"]*)\" subcommand$",
        (String subcmd) -> {
          final String appPath = System.getProperty("application.jar");

          if (Objects.equals("", appPath)) {
            throw new IllegalStateException("No application.jar system property defined");
          }

          Path startScript = Paths.get(appPath);

          ExecArgsBuilder argsBuilder =
              new ExecArgsBuilder().withStartScript(startScript).withArg(subcmd);

          LOGGER.info("Args {}", argsBuilder.build());
          final ProcessBuilder processBuilder = new ProcessBuilder(argsBuilder.build());

          process = processBuilder.start();

          int exitCode = process.waitFor();

          assertThat(exitCode).isZero();
        });

    Then(
        "^the distribution version is printed to stdout$",
        () -> {
          InputStream is = process.getInputStream();

          List<String> cmdOutput =
              new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                  .lines()
                  .collect(Collectors.toList());

          LOGGER.info("tessera version cmd output: {}", String.join("\n", cmdOutput));

          assertThat(cmdOutput)
              .describedAs(String.join(System.lineSeparator(), cmdOutput))
              .hasSize(1);
          capturedVersion = cmdOutput.get(0);
        });

    Then(
        "^the distribution version is in CalVer format$",
        () -> {
          assertThat(capturedVersion)
              .matches(Pattern.compile("^[0-9]{2}\\.[0-9]{1,2}\\.[0-9]+(-SNAPSHOT)?$"));
        });
  }
}
