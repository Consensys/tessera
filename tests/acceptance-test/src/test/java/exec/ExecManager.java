package exec;

import java.util.concurrent.ExecutorService;


import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ExecManager {

    Logger LOG = LoggerFactory.getLogger(ExecManager.class);

    Process doStart() throws Exception;

    void doStop() throws Exception;

    ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    
    default ExecutorService executorService() {
        return EXECUTOR_SERVICE;
    }
    
    default Process start() {
        try {
            return doStart();
        } catch (Exception ex) {
            LOG.error("", ex);
            throw new ExecException(ex);
        }
    }

    default void stop() {
        try {
            this.doStop();
        } catch (Exception ex) {
            LOG.warn("{}", ex.getMessage());
        } finally {
            EXECUTOR_SERVICE.shutdown();
        }

    }

}
