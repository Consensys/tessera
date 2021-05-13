package com.quorum.tessera.test.cli.keygen;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class FileKeygenStreamConsumer implements Runnable {

  private static final List<String> REQUEST_STRINGS =
      Arrays.asList(
          "Enter a password if you want to lock the private key or leave blank",
          "Please re-enter the password (or lack of) to confirm");

  private final InputStream inputStream;

  private final OutputStream outputStream;

  private final byte[] response;

  public FileKeygenStreamConsumer(
      final InputStream is, final OutputStream os, final String response) {
    this.inputStream = is;
    this.outputStream = os;
    this.response = (response + System.lineSeparator()).getBytes();
  }

  @Override
  public void run() {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (REQUEST_STRINGS.contains(line)) {
          outputStream.write(response);
          outputStream.flush();
        }
      }
    } catch (final IOException ex) {
      ex.printStackTrace();
    }
  }
}
