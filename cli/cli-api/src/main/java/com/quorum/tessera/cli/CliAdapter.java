package com.quorum.tessera.cli;

public interface CliAdapter {

  CliType getType();

  CliResult execute(String... args) throws Exception;
}
