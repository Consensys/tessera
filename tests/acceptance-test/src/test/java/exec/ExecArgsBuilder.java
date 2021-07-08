package exec;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ExecArgsBuilder {

  private Path configFile;

  private List<String> subcommands;

  private Path pidFile;

  private Path startScript;

  private final Map<String, String> argList = new LinkedHashMap<>();

  private final List<String> jvmArgList = new ArrayList<>();

  private final List<Path> classpathItems = new ArrayList<>();

  public ExecArgsBuilder withPidFile(Path pidFile) {
    this.pidFile = pidFile;
    return this;
  }

  public ExecArgsBuilder withJvmArg(String jvmArg) {
    this.jvmArgList.add(jvmArg);
    return this;
  }

  public ExecArgsBuilder withConfigFile(Path configFile) {
    this.configFile = configFile;
    return this;
  }

  public ExecArgsBuilder withStartScript(Path startScript) {
    this.startScript = startScript;
    return this;
  }

  public ExecArgsBuilder withSubcommands(String subcommand, String... s) {
    List<String> subcommands = new ArrayList<>();
    subcommands.add(subcommand);
    subcommands.addAll(Arrays.asList(s));

    this.subcommands = subcommands;
    return this;
  }

  public ExecArgsBuilder withArg(String name) {
    argList.put(name, null);
    return this;
  }

  public ExecArgsBuilder withArg(String name, String value) {
    argList.put(name, value);
    return this;
  }

  public ExecArgsBuilder withClassPathItem(Path classpathItem) {
    this.classpathItems.add(classpathItem);
    return this;
  }

  public List<String> build() {

    final List<String> tokens = new ArrayList<>();
    tokens.add(startScript.toAbsolutePath().toString());

    if (Objects.nonNull(subcommands)) {
      tokens.addAll(subcommands);
    }

    if (Objects.nonNull(configFile)) {
      tokens.add("-configfile");
      tokens.add(configFile.toAbsolutePath().toString());
    }

    if (Objects.nonNull(pidFile)) {
      tokens.add("-pidfile");
      tokens.add(pidFile.toAbsolutePath().toString());
    }

    argList
        .entrySet()
        .forEach(
            e -> {
              tokens.add(e.getKey());
              if (Objects.nonNull(e.getValue())) {
                tokens.add(e.getValue());
              }
            });

    return tokens;
  }

  public static void main(String[] args) throws Exception {
    List<String> argz =
        new ExecArgsBuilder()
            .withConfigFile(Paths.get("myconfig.json"))
            .withStartScript(Paths.get("ping"))
            .withJvmArg("-Dsomething=something")
            .withClassPathItem(Paths.get("/some.jar"))
            .withClassPathItem(Paths.get("lib").resolve("*"))
            .withArg("-jdbc.autoCreateTables", "true")
            .build();

    System.out.println(String.join(" ", argz));
  }
}
