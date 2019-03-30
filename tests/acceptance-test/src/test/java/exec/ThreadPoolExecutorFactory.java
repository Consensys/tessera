package exec;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public enum ThreadPoolExecutorFactory {

    INSTANCE;

    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 100,
        60L, TimeUnit.SECONDS,
        new SynchronousQueue<Runnable>());

    static ThreadPoolExecutor create() {
        return INSTANCE.threadPoolExecutor;

    }

}
