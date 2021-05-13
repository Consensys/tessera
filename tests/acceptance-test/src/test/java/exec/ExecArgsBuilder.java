package exec;

import com.quorum.tessera.config.Config;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ExecArgsBuilder {

  private Config config;

  private Path configFile;

  private List<String> subcommands;

  private Path pidFile;

  private Class mainClass;

  private Path executableJarFile;

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

  public ExecArgsBuilder withConfig(Config config) {
    this.config = config;
    return this;
  }

  public ExecArgsBuilder withConfigFile(Path configFile) {
    this.configFile = configFile;
    return this;
  }

  public ExecArgsBuilder withMainClass(Class mainClass) {
    this.mainClass = mainClass;
    return this;
  }

  public ExecArgsBuilder withStartScriptOrJarFile(Path file) {
    if (file.toFile().getName().toLowerCase().endsWith(".jar")) {
      return withClassPathItem(file);
    } else {
      return withStartScript(file);
    }
  }

  public ExecArgsBuilder withStartScriptOrExecutableJarFile(Path file) {
    if (file.toFile().getName().toLowerCase().endsWith(".jar")) {
      return withExecutableJarFile(file);
    } else {
      return withStartScript(file);
    }
  }

  private ExecArgsBuilder withExecutableJarFile(Path executableJarFile) {
    this.executableJarFile = executableJarFile;
    return this;
  }

  private ExecArgsBuilder withStartScript(Path startScript) {
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

    List<String> tokens = new ArrayList<>();

    if (startScript == null) {
      tokens.add("java");
      jvmArgList.forEach(tokens::add);
      if (!classpathItems.isEmpty()) {
        tokens.add("-cp");

        String classpathStr =
            classpathItems.stream()
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .collect(Collectors.joining(File.pathSeparator));
        tokens.add(classpathStr);
      }

      if (executableJarFile != null) {
        tokens.add("-jar");
        tokens.add(executableJarFile.toAbsolutePath().toString());
      } else {
        tokens.add(mainClass.getName());
      }

    } else {
      tokens.add(startScript.toAbsolutePath().toString());
    }

    if (Objects.nonNull(subcommands)) {
      tokens.addAll(subcommands);
    }

    tokens.add("-configfile");
    tokens.add(configFile.toAbsolutePath().toString());

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
            .withClassPathItem(Paths.get("/someother.jar"))
            .withArg("-o", "jdbc.autoCreateTables=true")
            .build();

    System.out.println(String.join(" ", argz));
  }
}
