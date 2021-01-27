package net.consensys.tessera.migration.data;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Mockito.*;

public class CompletionHandlerTest {

    private CompletionHandler completionHandler;

    @Before
    public void beforeTest() {
        completionHandler = new CompletionHandler();
    }


    @Test
    public void handle() throws Exception {
        OrionEvent orionEvent = mock(OrionEvent.class);
        when(orionEvent.getTotalEventCount()).thenReturn(1L);

        completionHandler.onEvent(orionEvent);
        completionHandler.await();

        verify(orionEvent).reset();
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

        completionHandler.await();

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


    }
}
