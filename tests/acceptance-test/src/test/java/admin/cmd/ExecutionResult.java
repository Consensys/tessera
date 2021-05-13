package admin.cmd;

import java.util.ArrayList;
import java.util.List;

public class ExecutionResult {

  private int exitCode;

  private final List<String> output = new ArrayList<>();

  private final List<String> errors = new ArrayList<>();

  public int getExitCode() {
    return exitCode;
  }

  public void setExitCode(int exitCode) {
    this.exitCode = exitCode;
  }

  public void addOutputLine(String line) {
    this.output.add(line);
  }

  public void addErrorLine(String line) {
    this.errors.add(line);
  }

  public List<String> getOutput() {
    return output;
  }

  public List<String> getErrors() {
    return errors;
  }
}
