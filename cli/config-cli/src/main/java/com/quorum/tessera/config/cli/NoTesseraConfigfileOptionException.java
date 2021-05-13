package com.quorum.tessera.config.cli;

import com.quorum.tessera.cli.CliException;

public class NoTesseraConfigfileOptionException extends CliException {
  public NoTesseraConfigfileOptionException() {
    super("Missing required option '--configfile <config>'");
  }
}
