package com.quorum.tessera.sync;

import java.net.URI;
import java.util.Objects;
import javax.websocket.Session;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SessionStoreTest {

    private SessionStore sessionStore;

    @Before
    public void onSetup() {
        sessionStore = SessionStore.create();
    }
    
    @After
    public void onTearDown() {
        sessionStore.clear();
        
        assertThat(sessionStore.find(Objects::nonNull)).isNotPresent();
    }

    @Test
    public void createFindAndRemove() {
        Session session = mock(Session.class);
        when(session.getId()).thenReturn("JUNIT");
        sessionStore.store(session);

        Session storedSession = sessionStore.find(Objects::nonNull).get();
        assertThat(session).isSameAs(storedSession);

        sessionStore.remove(session);

        assertThat(sessionStore.find(Objects::nonNull)).isNotPresent();

    }
    
    @Test
    public void findByUri() {
        
        URI uri = URI.create("http://somename.com");
        Session session = mock(Session.class);
        when(session.getId()).thenReturn("JUNIT");
        when(session.getRequestURI()).thenReturn(uri);
        
        sessionStore.store(session);

        Session storedSession = sessionStore.findByUri(uri);
        assertThat(session).isSameAs(storedSession);


    }
}
