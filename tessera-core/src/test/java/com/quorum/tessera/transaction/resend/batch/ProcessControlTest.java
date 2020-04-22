package com.quorum.tessera.transaction.resend.batch;

import com.quorum.tessera.transaction.resend.batch.ProcessControl;
import com.quorum.tessera.transaction.resend.batch.ProcessControlImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ProcessControlTest {

    private ProcessControl processControl;

    private ExecutorService executorService;

    private Runtime runtime;

    @Before
    public void onSetup() {
        executorService = mock(ExecutorService.class);
        runtime = mock(Runtime.class);
        processControl = new ProcessControlImpl(executorService, runtime);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(executorService, runtime);
    }

    @Test
    public void startAndStop() {

        Runnable toRun = mock(Runnable.class);
        Runnable shutdownHook = mock(Runnable.class);

        List<Thread> shutdownHandlers = new ArrayList<>();
        doAnswer(
                        (iom) -> {
                            shutdownHandlers.add(iom.getArgument(0));
                            return null;
                        })
                .when(runtime)
                .addShutdownHook(any(Thread.class));

        processControl.start(toRun, shutdownHook);

        verify(executorService).submit(toRun);

        processControl.exit(ProcessControl.SUCCESS);

        assertThat(shutdownHandlers).hasSize(1);

        verify(runtime).exit(ProcessControl.SUCCESS);
        verify(runtime).addShutdownHook(any(Thread.class));
    }

    @Test
    public void createDefaultInstance() {
        ProcessControl instance = ProcessControl.create();
        assertThat(instance).isExactlyInstanceOf(ProcessControlImpl.class);
    }
}
