package exec;

public interface ExecManager {

    Process doStart() throws Exception;

    void doStop() throws Exception;

    default Process start() {
        try {
            return doStart();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    default void stop() {
        try {
            this.doStop();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

}
