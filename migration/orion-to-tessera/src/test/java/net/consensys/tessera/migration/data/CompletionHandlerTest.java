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
        OrionDataEvent orionEvent = mock(OrionDataEvent.class);
        when(orionEvent.getTotalEventCount()).thenReturn(1L);

        completionHandler.onEvent(orionEvent,1L,false);
        completionHandler.await();

        verify(orionEvent).reset();
    }


    @Test
    public void handleMultiple() throws Exception {

        int total = 10;
        List<OrionDataEvent> events = IntStream.range(0,total)
            .mapToObj(i -> mock(OrionDataEvent.class))
            .collect(Collectors.toList());

        events.forEach(e -> {
            when(e.getTotalEventCount()).thenReturn((long) total);
        });

        for(OrionDataEvent orionEvent : events) {
            completionHandler.onEvent(orionEvent,1L,false);
            verify(orionEvent).reset();
        }
        completionHandler.await();
    }

    @Test
    public void handleMultipleButNotComplete() throws Exception {
        int total = 3;
        List<OrionDataEvent> events = IntStream.range(0,total)
            .mapToObj(i -> mock(OrionDataEvent.class))
            .collect(Collectors.toList());

        events.forEach(e -> {
            when(e.getTotalEventCount()).thenReturn((long) total);
        });

        for(int i = 0;i < 2;i++) {
            OrionDataEvent orionEvent = events.get(i);
            completionHandler.onEvent(orionEvent,1L,false);
            verify(orionEvent).reset();
        }
    }
}
