package config;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicInteger;

public class PortUtil {

  private AtomicInteger counter;

  public PortUtil(int initial) {
    counter = new AtomicInteger(initial);
  }

  public int nextPort() {

    while (true) {
      int port = counter.getAndIncrement();
      if (isLocalPortFree(port)) {
        return port;
      }
    }
  }

  private boolean isLocalPortFree(int port) {
    try {
      new ServerSocket(port).close();
      return true;
    } catch (IOException e) {
      return false;
    }
  }
}
