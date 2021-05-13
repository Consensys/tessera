package db;

public interface DatabaseServer {

  default void start() {}

  default void stop() {}
}
