package net.consensys.tessera.migration.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Mockito.*;

public class CompletionHandlerTest {

    private CompletionHandler completionHandler;

    private CountDownLatch countDownLatch;

    @Before
    public void beforeTest() {
        countDownLatch = spy(new CountDownLatch(1));
        completionHandler = new CompletionHandler(countDownLatch);
    }

    @After
    public void afterTest() {
        verifyNoMoreInteractions(countDownLatch);
    }

    @Test
    public void handle() throws Exception {
        OrionEvent orionEvent = mock(OrionEvent.class);
        when(orionEvent.getTotalEventCount()).thenReturn(1L);

        completionHandler.onEvent(orionEvent);

        verify(orionEvent).reset();
        verify(countDownLatch).countDown();

    }


    @Test
    public void handleMultiple() throws Exception {

        int total = 10;
        List<OrionEvent> events = IntStream.range(0,total)
            .mapToObj(i -> mock(OrionEvent.class))
            .collect(Collectors.toList());

        events.forEach(e -> {
            when(e.getTotalEventCount()).thenReturn((long) total);
        });


        for(OrionEvent orionEvent : events) {
            completionHandler.onEvent(orionEvent);
            verify(orionEvent).reset();
        }

        verify(countDownLatch).countDown();
    }

    @Test
    public void handleMultipleButNotComplete() throws Exception {
        int total = 3;
        List<OrionEvent> events = IntStream.range(0,total)
            .mapToObj(i -> mock(OrionEvent.class))
            .collect(Collectors.toList());

        events.forEach(e -> {
            when(e.getTotalEventCount()).thenReturn((long) total);
        });

        for(int i = 0;i < 2;i++) {
            OrionEvent orionEvent = events.get(i);
            completionHandler.onEvent(orionEvent);
            verify(orionEvent).reset();
        }

        verifyNoInteractions(countDownLatch);


    }
}
